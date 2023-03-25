package com.example.test000

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class TextVoice2SL : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textvoice2sl)


        val button3 = findViewById<Button>(R.id.switch_button)
        button3.setOnClickListener {
            val intent = Intent(this, Video2Text::class.java)
            startActivity(intent)
        }

        val button2 = findViewById<Button>(R.id.home_button)
        button2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}