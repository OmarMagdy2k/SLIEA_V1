package com.example.test000

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment

import android.provider.MediaStore
import android.view.View
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


class Video2Text : AppCompatActivity() {

    private var translationLanguage: String =
        "En" // default to "en" if intent extra is not available
    private lateinit var videoView: VideoView
    private var cameraRequestCode: Int = 123
    private var galleryRequestCode: Int = 122

    //Change Pages
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video2text)

        val languagesOptions = findViewById<ImageView>(R.id.languagesMenu)
        languagesOptions.setOnClickListener {
            val popupMenu = PopupMenu(this,it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.englishLanguage -> {
                        translationLanguage = "En"
                        true
                    }
                    R.id.arabicLanguage -> {
                        translationLanguage = "Ar"
                        true
                    }
                    else -> {false}
                }
            }
            popupMenu.inflate(R.menu.menu_main)
            popupMenu.show()
        }

        val signifyPageBtn = findViewById<ImageButton>(R.id.signifyBtn)
        signifyPageBtn.setOnClickListener {
            val signifyIntent = Intent(this,TextVoice2SL::class.java)
            startActivity(signifyIntent)
        }
        val tranSignPageBtn = findViewById<ImageButton>(R.id.tranSignBtn)
        tranSignPageBtn.setOnClickListener {
            val tranSignIntent = Intent(this,Image2Text::class.java)
            startActivity(tranSignIntent)
        }

        val tranSignBetaPageBtn = findViewById<ImageButton>(R.id.tranSignBetaBtn)
        tranSignBetaPageBtn.setOnClickListener {
            val tranSignBetaIntent = Intent(this,Video2Text::class.java)
            startActivity(tranSignBetaIntent)
        }

        val galleryBtn = findViewById<ImageButton>(R.id.galleryButton)
        galleryBtn.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(gallery, galleryRequestCode)
        }

        videoView = findViewById(R.id.record_videoView)
        // now set up media controller for the play pause next pre
        val mediaCollection = MediaController(this)
        mediaCollection.setAnchorView(videoView)
        videoView.setMediaController(mediaCollection)

    }

    fun startVideo(view: View) {
        //start intent to capture video
        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (videoIntent.resolveActivity(packageManager) != null) {
            videoIntent.putExtra(
                MediaStore.EXTRA_VIDEO_QUALITY,
                1
            ) // Set video quality (0 for low quality, 1 for high quality)
            startActivityForResult(videoIntent, cameraRequestCode)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            // get data from uri
            val videoUri = data?.data
            val videoFile = File(Environment.getExternalStorageDirectory(), "recorded_video.mp4")
            videoFile.createNewFile()
            val inputStream = contentResolver.openInputStream(videoUri!!)
            val outputStream = FileOutputStream(videoFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input?.copyTo(output)
                }
                videoView.setVideoURI(videoUri)
                videoView.start()
            }
        }
        if (requestCode == galleryRequestCode && resultCode == RESULT_OK) {
            val contentUri: Uri? = data?.data
            videoView.setVideoURI(contentUri)
        }
    }
}