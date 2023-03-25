package com.example.test000

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView


class Video2Text : AppCompatActivity() {

private lateinit var videoView: VideoView
private var ourRequestCode : Int = 123 // any number

    //Change Pages
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video2text)

        val button1 = findViewById<Button>(R.id.switch_button)
        button1.setOnClickListener {
            val intent = Intent(this, TextVoice2SL::class.java)
            startActivity(intent)

        }

        val button2 = findViewById<Button>(R.id.home_button)
        button2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        videoView = findViewById<VideoView>(R.id.record_videoView)
        // now set up media controller for the play pause next pre

        val mediaCollection = MediaController(this)
        mediaCollection.setAnchorView(videoView)
        videoView.setMediaController(mediaCollection)

    }

    fun startVideo(view: View) {
        //start intent to capture video
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if(intent.resolveActivity(packageManager)!=null){
            startActivityForResult(intent,ourRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ourRequestCode && resultCode == RESULT_OK){
            // get data from uri
            val videoUri = data?.data
            videoView.setVideoURI(videoUri)
            videoView.start()
        }
    }
}