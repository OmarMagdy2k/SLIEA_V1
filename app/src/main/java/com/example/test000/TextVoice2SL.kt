package com.example.test000

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class TextVoice2SL : AppCompatActivity() {
    private var translationLanguage: String =
        "En" // default to "en" if intent extra is not available
    private lateinit var documentRef: DocumentReference
    private var storedVideos = mutableListOf<File>()
    private lateinit var correctedStatement: String
    private lateinit var wordsList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textvoice2sl)

        val intent = intent

        // Check if the intent contains an extra with key "translationLanguage"
        if (intent.hasExtra("translationLanguage")) {
            translationLanguage = intent.getStringExtra("translationLanguage")!!
        }

        val switchBtn = findViewById<ImageButton>(R.id.switch_button)
        switchBtn.setOnClickListener {
            val intent = Intent(this, Image2Text::class.java)
            startActivity(intent)
        }

        val homeBtn = findViewById<ImageButton>(R.id.home_button)
        homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val editText = findViewById<EditText>(R.id.editTextTextMultiLine)

        val transButton = findViewById<ImageButton>(R.id.trans_button)
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
            playVideos()
        }
    }

    private fun startSpeechToText(editText: EditText) {
        // Request permission to use the microphone
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return
        }

        // Create a SpeechRecognizer instance
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Create an intent for speech recognition
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        // Set the language code based on the translationLanguage variable
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            if (translationLanguage == "Ar") "ar-SA" else "en-US"
        )
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

    private fun transText(editText: EditText) {
        val userInput = editText.text.toString().lowercase()
        if (translationLanguage == "En") {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            val py = Python.getInstance()
            val module: PyObject = py.getModule("client")
            try {
                correctedStatement = module.callAttr("autocorrect_En", userInput)
                    .toJava(String::class.java)
                editText.setText(correctedStatement)

            } catch (e: PyException) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
            wordsList = correctedStatement.split("\\s+".toRegex())
        } else if (translationLanguage == "Ar") {
            wordsList = userInput.split("\\s+".toRegex())
        }
        val firestore =
            FirebaseFirestore.getInstance() // Replace with your actual Firestore reference

        // Create a query to search for the value in a specific collection
        if (translationLanguage == "En") {
            documentRef = firestore.collection("English_Videos").document("Videos")
        } else if (translationLanguage == "Ar") {
            documentRef = firestore.collection("Arabic_Videos").document("Videos")
        }
        documentRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                storedVideos.clear()
                for (word in wordsList) {
                    // Retrieve the value of the field from the document snapshot
                    val videoUrl = documentSnapshot.data?.get(word)?.toString()
                    if (videoUrl != null) {
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                        val videoFileName = "${word}_${timeStamp}.mp4"
                        val success = downloadVideoFromDrive(videoUrl, videoFileName)
                    } else {
                        Toast.makeText(this, "Can't translate this message", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                playVideos();
            } else {
                Toast.makeText(this, "Can't translate this message", Toast.LENGTH_SHORT).show()
                //Log.d("Firestore Value", "Document not found")
            }
        }.addOnFailureListener { exception ->
            // Handle any errors that occur during the retrieval
            Log.e("Firestore Value", "Error: ${exception.message}")
        }
    }

    @SuppressLint("Range")
    private fun downloadVideoFromDrive(driveUrl: String, fileName: String): Boolean {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(driveUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(false)
        request.setTitle(fileName)
        request.setDescription("Downloading video")
        request.setMimeType("video/*")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        downloadManager.enqueue(request)
        var query = DownloadManager.Query()
        query.setFilterByStatus(DownloadManager.STATUS_FAILED or DownloadManager.STATUS_PAUSED or DownloadManager.STATUS_SUCCESSFUL or DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PENDING)
        var downloading = true
        var success = false
        while (downloading) {
            val c = downloadManager.query(query)
            if (c.moveToFirst()) {
                Log.i("FLAG", "Downloading")
                val status: Int = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    Log.i("FLAG", "done")
                    downloading = false
                    val videoFile = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    )
                    if (videoFile.exists()) {
                        storedVideos.add(videoFile)
                        success = true
                    }
                    break
                }
                if (status == DownloadManager.STATUS_FAILED) {
                    Log.i("FLAG", "Fail")
                    downloading = false
                    success = false
                    break
                }
            }
            c.close()
        }
        return success
    }

    private fun playVideos() {
        val videoView = findViewById<VideoView>(R.id.graphic_videoView)
        if (storedVideos.isNotEmpty()) {
            var index = 0
            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            val videoUri = Uri.parse(storedVideos[index].absolutePath)
            videoView.setVideoURI(videoUri)
            videoView.requestFocus()
            videoView.setOnCompletionListener {
                index++
                if (index < storedVideos.size) {
                    val nextVideoUri = Uri.parse(storedVideos[index].absolutePath)
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
