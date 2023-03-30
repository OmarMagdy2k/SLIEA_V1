package com.example.test000

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val confirmButton = findViewById<Button>(R.id.confirm_button1)
        val processGroup = findViewById<RadioGroup>(R.id.processGroup)

        processGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.vt2slButton -> {
                    confirmButton.setOnClickListener {
                        val intent = Intent(this,TextVoice2SL::class.java)
                        startActivity(intent)
                    }
                }
                R.id.slv2vtButton -> {
                    confirmButton.setOnClickListener {
                        val intent = Intent(this,Video2Text::class.java)
                        startActivity(intent)
                    }
                }
                R.id.slp2vtButton -> {
                    confirmButton.setOnClickListener {
                        val intent = Intent(this,Image2Text::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}