package com.example.test000

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.*


class TextVoice2SL : AppCompatActivity() {
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textvoice2sl)

        val button3 = findViewById<Button>(R.id.switch_button)
        button3.setOnClickListener {
            val intent = Intent(this, Image2Text::class.java)
            startActivity(intent)
        }

        val button2 = findViewById<Button>(R.id.home_button)
        button2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val editText = findViewById<EditText>(R.id.editTextTextMultiLine)
        val transButton = findViewById<Button>(R.id.trans_button)
        transButton.setOnClickListener {
            transText(editText)

        }
    }

    private fun transText(editText:EditText ) {
        val userInput = editText.text.toString()
        val wordsList = userInput.split("\\s+".toRegex())

        val firestore = FirebaseFirestore.getInstance() // Replace with your actual Firestore reference

        // Create a query to search for the value in a specific collection
        val documentRef = firestore.collection("English_Videos").document("Videos")
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
                    handler.postDelayed(this, 3000) // Poll every 3 second (adjust as needed)
                } else {
                    // All downloads are complete
                    cursor.close()
                    openVideo(fileName) // call openVideo function here
                }
            }
        }, 3000) // Delay the first poll by 3 second (adjust as needed)
    }


    private fun openVideo(fileName: String) {
        val videoView = findViewById<VideoView>(R.id.graphic_videoView)
        val videoFiles = mutableListOf<File>()
        for (index in 1..10) {
            val videoFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "${fileName}_$index.mp4")
            if (videoFile.exists()) {
                videoFiles.add(videoFile)
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
            videoView.setOnCompletionListener { mp ->
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



}





//    private fun transText(editText: EditText) {
//        val firestore = FirebaseFirestore.getInstance() // Replace with your actual Firestore reference
//        val userInput = editText.text.toString()
//        val wordsList = userInput.split("\\s+".toRegex())
//
//        // Create a query to search for the value in a specific collection
//        val documentRef = firestore.collection("English_Videos").document("Videos")
//        documentRef.get().addOnSuccessListener { documentSnapshot ->
//            if (documentSnapshot.exists()) {
//                // Retrieve the value of the field from the document snapshot
//                val videoUrl = documentSnapshot.data?.get(userInput).toString()
//                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//                val videoFileName = "${userInput}_${timeStamp}.mp4"
//                // Handle the retrieved value as needed
//                downloadVideoFromDrive(videoUrl,videoFileName)
//                openVideo(videoFileName)
//            } else {
//                Toast.makeText(this, "Can't Translate this message", Toast.LENGTH_SHORT).show()
//                //Log.d("Firestore Value", "Document not found")
//            }
//        }.addOnFailureListener { exception ->
//            // Handle any errors that occur during the retrieval
//            Log.e("Firestore Value", "Error: ${exception.message}")
//        }
//
//    }
//    private fun downloadVideoFromDrive(driveUrl: String, fileName: String) {
//        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
//        val request = DownloadManager.Request(Uri.parse(driveUrl))
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//        request.setAllowedOverRoaming(false)
//        request.setTitle(fileName)
//        request.setDescription("Downloading video")
//        request.setMimeType("video/*")
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        val downloadId = downloadManager.enqueue(request)
//
//        handler.postDelayed(object : Runnable {
//            @SuppressLint("Range")
//            override fun run() {
//                val query = DownloadManager.Query()
//                query.setFilterById(downloadId)
//                val cursor = downloadManager.query(query)
//                if (cursor.moveToFirst()) {
//                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
//                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                        // Video file downloaded successfully
//                        cursor.close()
//                        openVideo(fileName)
//                    } else {
//                        // Handle download failure, e.g., show an error message
//                        cursor.close()
//                        // Poll again after a delay
//                        handler.postDelayed(this, 1000) // Poll every 1 second (adjust as needed)
//                    }
//                }
//            }
//        }, 1000) // Delay the first poll by 1 second (adjust as needed)
//    }
//
//    private fun openVideo(fileName: String){
//        val videoView = findViewById<VideoView>(R.id.graphic_videoView)
//        val videoFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileName)
//        if (videoFile.exists()) {
//            // Set the video path and start playback
//            videoView.setVideoPath(videoFile.absolutePath)
//            val mediaController = MediaController(this)
//            mediaController.setAnchorView(videoView)
//            videoView.setMediaController(mediaController)
//            videoView.start()
//        } else {
//            //Toast.makeText(this, "Video file not found", Toast.LENGTH_SHORT).show()
//        }
//    }

