package com.example.test000

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.*
import android.Manifest
import com.google.firebase.firestore.DocumentReference


class TextVoice2SL : AppCompatActivity() {
    private val handler = Handler()
    private var translationLanguage: String = "En" // default to "en" if intent extra is not available
    private lateinit var documentRef : DocumentReference
    private var storedVideos = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textvoice2sl)

        val intent = intent

        // Check if the intent contains an extra with key "translationLanguage"
        if (intent.hasExtra("translationLanguage")) {
            translationLanguage = intent.getStringExtra("translationLanguage")!!
        }

        val switchBtn = findViewById<Button>(R.id.switch_button)
        switchBtn.setOnClickListener {
            val intent = Intent(this, Image2Text::class.java)
            startActivity(intent)
        }

        val homeBtn = findViewById<Button>(R.id.home_button)
        homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val editText = findViewById<EditText>(R.id.editTextTextMultiLine)
        val transButton = findViewById<Button>(R.id.trans_button)
        transButton.setOnClickListener {
            transText(editText)
        }

        val speech2TxtBtn = findViewById<ImageButton>(R.id.micButton)
        speech2TxtBtn.setOnClickListener {
                startSpeechToText(editText)
                Toast.makeText(this, "Start Listening", Toast.LENGTH_SHORT).show()
        }

        val replayBtn = findViewById<ImageButton>(R.id.replayButton)
        replayBtn.setOnClickListener {
            replayVideo(storedVideos)
        }
    }

    private fun startSpeechToText(editText: EditText) {
        // Request permission to use the microphone
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return
        }

        // Create a SpeechRecognizer instance
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Create an intent for speech recognition
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        // Set the language code based on the translationLanguage variable
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (translationLanguage == "Ar") "ar-SA" else "en-US")
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        // Set up a listener for the recognition results
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {}

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    editText.setText(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        // Start listening for speech
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    private fun transText(editText:EditText ) {
        val userInput = editText.text.toString().lowercase()

        val wordsList = userInput.split("\\s+".toRegex())

        val firestore = FirebaseFirestore.getInstance() // Replace with your actual Firestore reference

        // Create a query to search for the value in a specific collection
        if(translationLanguage == "En"){
             documentRef = firestore.collection("English_Videos").document("Videos")
        } else if(translationLanguage == "Ar"){
            documentRef = firestore.collection("Arabic_Videos").document("Videos")
        }
        documentRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val videoUrls = mutableListOf<String>()
                for (word in wordsList) {
                    // Retrieve the value of the field from the document snapshot
                    val videoUrl = documentSnapshot.data?.get(word)?.toString()
                    if (videoUrl != null) {
                        videoUrls.add(videoUrl)
                    }
                }
                if (videoUrls.isNotEmpty()) {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val videoFileName = "${wordsList.joinToString("_")}_${timeStamp}.mp4"
                    // Handle the retrieved value as needed
                    downloadVideoFromDrive(videoUrls, videoFileName)
                    openVideo(videoFileName)
                } else {
                    Toast.makeText(this, "Can't translate this message", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Can't translate this message", Toast.LENGTH_SHORT).show()
                //Log.d("Firestore Value", "Document not found")
            }
        }.addOnFailureListener { exception ->
            // Handle any errors that occur during the retrieval
            Log.e("Firestore Value", "Error: ${exception.message}")
        }
    }

    private fun downloadVideoFromDrive(driveUrls: List<String>, fileName: String) {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        for ((index, driveUrl) in driveUrls.withIndex()) {
            val request = DownloadManager.Request(Uri.parse(driveUrl))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.setTitle("${fileName}_${index+1}")
            request.setDescription("Downloading video")
            request.setMimeType("video/*")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${fileName}_${index+1}.mp4")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            downloadManager.enqueue(request)
        }
        handler.postDelayed(object : Runnable {
            @SuppressLint("Range")
            override fun run() {
                val query = DownloadManager.Query()
                query.setFilterByStatus(DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PAUSED or DownloadManager.STATUS_PENDING)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    // There are downloads still in progress
                    cursor.close()
                    // Poll again after a delay
                    handler.postDelayed(this, 1000) // Poll every 1 second (adjust as needed)
                } else {
                    // All downloads are complete
                    cursor.close()
                    openVideo(fileName) // call openVideo function here
                }
            }
        }, 1000) // Delay the first poll by 1 second (adjust as needed)
    }
    private fun openVideo(fileName: String) {
        val videoView = findViewById<VideoView>(R.id.graphic_videoView)
        val videoFiles = mutableListOf<File>()
        for (index in 1..200) {
            val videoFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "${fileName}_$index.mp4")
            if (videoFile.exists()) {
                videoFiles.add(videoFile)
                storedVideos.add(videoFile)
            }
        }
        if (videoFiles.isNotEmpty()) {
            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            // Set video URI to the first video file
            val videoUri = Uri.parse(videoFiles[0].absolutePath)
            videoView.setVideoURI(videoUri)
            videoView.requestFocus() // Request focus for the VideoView
            videoView.setOnCompletionListener {
                // Remove the first video file from the list
                videoFiles.removeAt(0)
                if (videoFiles.isNotEmpty()) {
                    // Set the video URI to the next file in the list
                    val nextVideoUri = Uri.parse(videoFiles[0].absolutePath)
                    videoView.setVideoURI(nextVideoUri)
                    videoView.start()
                }
            }
            videoView.start()
        } else {
            //Toast.makeText(this, "No video files found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replayVideo(storedVideo: List<File>) {
        val videoView = findViewById<VideoView>(R.id.graphic_videoView)
        if (storedVideo.isNotEmpty()) {
            var index = 0
            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            val videoUri = Uri.parse(storedVideo[index].absolutePath)
            videoView.setVideoURI(videoUri)
            videoView.requestFocus()
            videoView.setOnCompletionListener {
                index++
                if (index < storedVideo.size) {
                    val nextVideoUri = Uri.parse(storedVideo[index].absolutePath)
                    videoView.setVideoURI(nextVideoUri)
                    videoView.start()
                }
            }
            // only start the video here once all listeners have been set up
            videoView.start()
        } else {
            // Toast.makeText(this, "No video files found", Toast.LENGTH_SHORT).show()
        }
    }
}
