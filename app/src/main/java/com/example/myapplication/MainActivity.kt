 package com.example.myapplication


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private var takePictureButton: ImageButton? = null
//    lateinit var imageview: ImageView
    private var cameraId: String? = null
    protected var cameraDevice: CameraDevice? = null
    protected var cameraCaptureSessions: CameraCaptureSession? = null
//    protected var captureRequest: CaptureRequest? = null
    protected var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private val file: File? = null
//    private val mFlashSupported = false
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
//    var list = ArrayList<Uri>()
    val REQUEST_CODE = 200
    lateinit var gridView: GridView
    lateinit var uploadimg: ImageButton
    private var textureView: TextureView? = null
    val CR = 0
    val hlistViewModel = listViewModel()


    companion object {

        var text_list = ArrayList<String>()
//        var x =0
        private const val TAG = "AndroidCameraApi"
        private val ORIENTATIONS = SparseIntArray()
        private const val REQUEST_CAMERA_PERMISSION = 200
        fun decodeSampledBitmapFromFile(
                path: String?,
                reqWidth: Int, reqHeight: Int
        ): Bitmap {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            //Query bitmap without allocating memory
            options.inJustDecodeBounds = true
            //decode file from path
            BitmapFactory.decodeFile(path, options)
            // Calculate inSampleSize
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            //decode according to configuration or according best match
            options.inPreferredConfig = Bitmap.Config.RGB_565
            var inSampleSize = 1
            if (height > reqHeight) {
                inSampleSize = Math.round(height.toFloat() / reqHeight.toFloat())
            }
            val expectedWidth = width / inSampleSize
            if (expectedWidth > reqWidth) {
//                if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
                inSampleSize = Math.round(width.toFloat() / reqWidth.toFloat())
            }
            //if value is greater than 1,sub sample the original image
            options.inSampleSize = inSampleSize
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(path, options)
        }
        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gridView = findViewById(R.id.list_item) as GridView

        updateAdapte()

        title = "home"
        uploadimg = findViewById(R.id.button2)
        uploadimg.setOnClickListener {
            openGalleryForImages()
        }


        gridView = findViewById(R.id.list_item) as GridView

        textureView = findViewById<TextureView>(R.id.texture)

        assert(textureView != null)
        textureView!!.surfaceTextureListener = textureListener
        takePictureButton = findViewById<ImageButton>(R.id.btn_takepicture)
        assert(takePictureButton != null)

        try {
            takePictureButton!!.setOnClickListener {
                takePicture()
            }
        } catch (e: Exception) {
            println("......................$e......................")
        }

    }

    fun updateAdapte(){
        val adapter1=  listAdapter(this, text_list)
        gridView.adapter = adapter1
    }
    var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
        ) {
            openCamera()
        }


        override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
        ) {
            // Transform you image captured size according to the surface width and height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }
    private val stateCallback: CameraDevice.StateCallback =
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    //This is called when the camera is open
                    Log.e(Companion.TAG, "onOpened")
                    cameraDevice = camera
                    createCameraPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cameraDevice!!.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    cameraDevice!!.close()
                    cameraDevice = null
                }
            }
    val captureCallbackListener: CaptureCallback = object : CaptureCallback() {
        override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            Toast.makeText(this@MainActivity, "Saved:$file", Toast.LENGTH_SHORT).show()
            createCameraPreview()
        }
    }

    protected fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    protected fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun takePicture() {
                if (null == cameraDevice) {
            Log.e(Companion.TAG, "cameraDevice is null")
            return
        }
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(
                    cameraDevice!!.id
            )
            var jpegSizes: Array<Size>? = null
            if (characteristics != null) {
                jpegSizes =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                                .getOutputSizes(ImageFormat.JPEG)
            }
            var width = 640
            var height = 480
            if (jpegSizes != null && 0 < jpegSizes.size) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }
            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces: MutableList<Surface> = ArrayList(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView!!.surfaceTexture))
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(
                    CaptureRequest.CONTROL_MODE,
                    CameraMetadata.CONTROL_MODE_AUTO
            )
            // Orientation
            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder.set(
                    CaptureRequest.JPEG_ORIENTATION,
                    Companion.ORIENTATIONS[rotation]
            )

            val file =
                    File(
                            Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DCIM
                            ).toString() + "/pic.jpg"
                    )

            val readerListener: OnImageAvailableListener =
                    object : OnImageAvailableListener {
                        override fun onImageAvailable(reader: ImageReader) {
                            var image: Image? = null
                            try {
                                image = reader.acquireNextImage()
                                val buffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.capacity())
                                buffer[bytes]
                                save(bytes)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            } finally {
                                image?.close()
                            }
                        }

                        @Throws(IOException::class)
                        private fun save(bytes: ByteArray) {
                            var output: OutputStream? = null
                            try {
                                output = FileOutputStream(file.toString())
                                output.write(bytes)
                            } finally {
                                output?.close()
                            }
                            try {

                                val PATH = file.path
                                val f = File(PATH)
                                val yourUri = Uri.fromFile(f)

                                hlistViewModel.text_qqq = text_list

                                val intent = Intent(this@MainActivity, testActivity::class.java).apply {}
                                intent.putExtra("camera_photo",(File(file.path)).toString())

                                startActivityForResult(intent,CR)

                            } catch (e: Exception) {
                                println("--------------save image  Exception-----------$e------------------------")
                            }
                        }
                    }

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    Toast.makeText(this@MainActivity, "Saved:$file", Toast.LENGTH_SHORT)
                            .show()
                    createCameraPreview()
                }
            }
            cameraDevice!!.createCaptureSession(
                    outputSurfaces,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            try {
                                session.capture(
                                        captureBuilder.build(),
                                        captureListener,
                                        mBackgroundHandler
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    },
                    mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    protected fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder =
                    cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice!!.createCaptureSession(
                    Arrays.asList(surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
                            //The camera is already closed
                            if (null == cameraDevice) {
                                return
                            }
                            // When the session is ready, we start displaying the preview.
                            cameraCaptureSessions = cameraCaptureSession
                            updatePreview()
                        }

                        override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {
                            Toast.makeText(
                                    this@MainActivity,
                                    "Configuration change",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        Log.e(Companion.TAG, "is camera open")
        try {
            cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(manager.cameraIdList[0])
            val map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                    ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) !== PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        Companion.REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(manager.cameraIdList[0], stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        Log.e(Companion.TAG, "openCamera X")
    }

    protected fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(Companion.TAG, "updatePreview error, return")
        }
        captureRequestBuilder!!.set(
                CaptureRequest.CONTROL_MODE,
                CameraMetadata.CONTROL_MODE_AUTO
        )
        try {
            cameraCaptureSessions!!.setRepeatingRequest(
                    captureRequestBuilder!!.build(),
                    null,
                    mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            println(e.toString())

            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        if (null != cameraDevice) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != imageReader) {
            imageReader!!.close()
            imageReader = null
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == Companion.REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(
                        this@MainActivity,
                        "Sorry!!!, you can't use this app without granting permission",
                        Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(Companion.TAG, "onResume")
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera()
        } else {
            textureView!!.surfaceTextureListener = textureListener
        }
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state


    }


    private fun openGalleryForImages() {

        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                    Intent.createChooser(intent, "Choose Pictures"), REQUEST_CODE
            )

        } else { // For latest versions API LEVEL 19+
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE);
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {

            // if multiple images are selected
            if (data?.clipData != null) {
                var count = data.clipData?.itemCount

                if (count != null) {
                    if (count > 5) count = 5
                }
                for (i in 0..count!! - 1) {
                    var imageUri: Uri = data.clipData?.getItemAt(i)!!.uri
                    text_list.add(imageUri.toString())
                    hlistViewModel.text_qqq = text_list
                    updateAdapte()
                }

            } else if (data?.data != null) {
//                 if single image is selected

                var imageUri: Uri = data.data!!

                text_list.add(imageUri.toString())
                hlistViewModel.text_qqq = text_list
                updateAdapte()

            }
        }
        if (resultCode == RESULT_OK && requestCode == CR) {

                val extras = intent.extras
                text_list.add(data?.getStringExtra("img").toString())
                hlistViewModel.text_qqq = text_list
                 updateAdapte()



        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    }











