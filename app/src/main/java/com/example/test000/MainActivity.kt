package com.example.test000

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var translationLanguage: String
    private var processSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val confirmButton = findViewById<Button>(R.id.confirm_button1)
        confirmButton.isEnabled = false // disable the button initially

        val languageGroup = findViewById<RadioGroup>(R.id.languageGroup)
        languageGroup.setOnCheckedChangeListener { _, checkedLang ->
            when (checkedLang) {
                R.id.englishButton -> {
                    translationLanguage = "En"
                }
                R.id.arabicButton -> {
                    translationLanguage = "Ar"
                }
            }
            updateConfirmButtonState(confirmButton)
        }
        val processGroup = findViewById<RadioGroup>(R.id.processGroup)
        processGroup.setOnCheckedChangeListener { _, _ ->
            processSelected = true
            updateConfirmButtonState(confirmButton)
        }

        confirmButton.setOnClickListener {
            val intent = when (processGroup.checkedRadioButtonId) {
                R.id.vt2slButton -> {
                    Intent(this, TextVoice2SL::class.java)
                }
                R.id.slv2vtButton -> {
                    Intent(this, Video2Text::class.java)
                }
                R.id.slp2vtButton -> {
                    Intent(this, Image2Text::class.java)
                }
                else -> null
            }
            intent?.putExtra("translationLanguage", translationLanguage)
            startActivity(intent)
                }
            }

    private fun updateConfirmButtonState(confirmButton:Button) {
        confirmButton.isEnabled = translationLanguage != null && processSelected
    }
}