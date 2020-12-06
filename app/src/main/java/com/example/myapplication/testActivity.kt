package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File


class  testActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var repet : Button
    lateinit var donephoto : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val extras = intent.extras

        repet = findViewById(R.id.repet)
        donephoto = findViewById(R.id.donee)
        imageView = findViewById(R.id.imageView4)


        if (extras != null) {

        imageView.setImageURI(Uri.parse(extras.getString("camera_photo")))
             }

        repet.setOnClickListener {
            val intent = Intent(this@testActivity, MainActivity::class.java).apply {}
            intent.putExtra("img", extras?.getString("camera_photo"))
            setResult(54,intent)
            finish()
        }

        donephoto.setOnClickListener {
            val intent = Intent(this@testActivity, MainActivity::class.java).apply {}
            intent.putExtra("img", extras?.getString("camera_photo"))
            setResult(RESULT_OK,intent)
            finish()


        }

    }
}