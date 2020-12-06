package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class listViewModel() {

//    private val sharedPrefFile = "kotlinsharedpreference"
//    val sharedPreferences: SharedPreferences = context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
//    val editor: SharedPreferences.Editor = sharedPreferences.edit()

//    for (i in sharedPrefFile) {
//        val sharedNameValue = sharedPreferences.getString("img" + "${x}", y)
//        x++
//        if (sharedNameValue != null) {
//            text_lis.add(sharedNameValue)
//        }
//    }

        var text_qqq = ArrayList<String>()
            set(str) {
                field  = str
            }
            get () = field
    }

