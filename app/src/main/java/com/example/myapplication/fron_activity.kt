package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class fron_activity : AppCompatActivity() {
    lateinit var btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fron_activity)


        btn = findViewById(R.id.button2)
        btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }
    }
}