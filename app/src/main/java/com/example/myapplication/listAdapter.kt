package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import java.io.File

var y = null
var x = 0


class listAdapter (private val context: Context,
                   private val text_list: ArrayList<String>) : BaseAdapter() {

    val h = listViewModel()

    lateinit var img: ImageView
    lateinit var deletbtn: ImageButton

    override fun getCount(): Int {
        return text_list.size
    }

    override fun getItem(position: Int): Any {
        return 5
    }

    override fun getItemId(position: Int): Long {
        return 2
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        val rowView = LayoutInflater.from(context).inflate(R.layout.list_design, null, false)

        img = rowView.findViewById(R.id.imageView4)
        deletbtn = rowView.findViewById(R.id.deleimg)
        notifyDataSetChanged()

        try {
            img.setImageURI(Uri.parse(text_list.get(position)))

        } catch (e: Exception) {
            println("==============$e=======================")
        }

        img.setOnClickListener() {

            val intent = Intent(context, details_actigity::class.java)
            intent.putExtra("img", text_list.get(position))
            startActivity(context, intent, Bundle())
            notifyDataSetChanged()

        }
        deletbtn.setOnClickListener {
            text_list.removeAt(position)
            notifyDataSetChanged()
        }
        return rowView
    }
}


