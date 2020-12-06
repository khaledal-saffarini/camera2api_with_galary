package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageSwitcher
import android.widget.ImageView

class details_actigity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_actigity)
        var text_lis = ArrayList<String>()
        var h = listViewModel()
        val sharedPrefFile = "kotlinsharedpreference"
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, MODE_PRIVATE)

        val extras = intent.extras
        var x = 0
        var y = null
        val imgSwitcher = findViewById<ImageSwitcher>(R.id.imgSw)
//        val imageView: ImageView = findViewById<ImageView>(R.id.imgSw)

        val next: ImageButton = findViewById<ImageButton>(R.id.next)
        val prev: ImageButton = findViewById<ImageButton>(R.id.prev)
        val returnbtn: Button = findViewById<Button>(R.id.back)
        val delebtn: ImageButton = findViewById<ImageButton>(R.id.deleimg)

//        var a = extras?.getString("img")
        for (i in sharedPrefFile) {
            val sharedNameValue = sharedPreferences.getString("img" + "${x}", y)
            x++
            if (sharedNameValue != null) {
                text_lis.add(sharedNameValue)
            }
        }

        val a = extras?.getString("img")
        var index = text_lis.indexOf(a)
        println("************************************************")
        println(text_lis.size)

        println(index)

        imgSwitcher?.setFactory {
            val imgView = ImageView(applicationContext)
            imgView.scaleType = ImageView.ScaleType.CENTER_CROP
            imgView
        }

        imgSwitcher?.setImageURI(Uri.parse(a))

        val imgIn = AnimationUtils.loadAnimation(
                this, android.R.anim.slide_in_left)
        imgSwitcher?.inAnimation = imgIn

        val imgOut = AnimationUtils.loadAnimation(
                this, android.R.anim.slide_out_right)
        imgSwitcher?.outAnimation = imgOut

        prev.setOnClickListener {
            index = if (index - 1 < 0) text_lis.size-1 else index - 1
            println(index)
            imgSwitcher?.setImageURI(Uri.parse(text_lis.get(index)))
        }
        next.setOnClickListener {
            index = if (index + 1 > text_lis.size-1) 0  else index + 1
            imgSwitcher?.setImageURI(Uri.parse(text_lis.get(index)))


        }
        returnbtn.setOnClickListener {
            val intent = Intent(this@details_actigity, MainActivity::class.java)
            startActivity(intent)

        }
        delebtn.setOnClickListener {
            text_lis.removeAt(index)
            index = if (index + 1 > text_lis.size-1) 0  else index + 1
            imgSwitcher?.setImageURI(Uri.parse(text_lis.get(index)))
        }
    }
}




