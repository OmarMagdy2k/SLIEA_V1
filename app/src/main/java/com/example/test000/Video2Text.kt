package com.example.test000

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment

import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import androidx.core.content.FileProvider
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class Video2Text : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private var cameraRequestCode: Int = 123
    private var galleryRequestCode: Int = 122
    private lateinit var txtTranslated: TextView
    private lateinit var tts: TextToSpeech
    private lateinit var pythonModule: PyObject
    private lateinit var cameraVideo: File

    internal var currentTranslationLanguage : String = ""

    //Change Pages
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video2text)

        val intent = intent
        // Check if the intent contains an extra with key "translationLanguage"
        if (intent.hasExtra("currentTranslationLanguage")) {
            currentTranslationLanguage = intent.getStringExtra("currentTranslationLanguage")!!
        }

        val languagesOptions = findViewById<ImageView>(R.id.languagesMenu)
        languagesOptions.setOnClickListener {
            val popupMenu = PopupMenu(this,it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.englishLanguage -> {
                        currentTranslationLanguage = "En"
                        true
                    }
                    R.id.arabicLanguage -> {
                        currentTranslationLanguage = "Ar"
                        true
                    }
                    else -> {false}
                }
            }
            popupMenu.inflate(R.menu.menu_main)
            popupMenu.show()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.tranSignBetaBtn

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.signifyBtn -> {
                    val int = Intent(applicationContext, TextVoice2SL::class.java)
                    int.putExtra("currentTranslationLanguage",currentTranslationLanguage)
                    startActivity(int)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.tranSignBtn -> {
                    val int = Intent(applicationContext, Image2Text::class.java)
                    int.putExtra("currentTranslationLanguage",currentTranslationLanguage)
                    startActivity(int)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.tranSignBetaBtn -> true
                else -> false
            }
        }

        tts = TextToSpeech(applicationContext) { status ->
            if (status != TextToSpeech.ERROR) {
                if(currentTranslationLanguage != ""){
                    if (currentTranslationLanguage == "En") {
                        // Set language for TextToSpeech
                        tts.language = Locale.US
                    } else if (currentTranslationLanguage == "Ar") {
                        tts.language = Locale("ar")
                    }
                } else {
                    tts.language = Locale.US
                }
            }
        }
        txtTranslated = findViewById(R.id.out_trans_text)


        val videoBtn = findViewById<ImageButton>(R.id.videoButton)
        videoBtn.setOnClickListener {
            dispatchTakeVideoIntent()
        }

        val galleryBtn = findViewById<ImageButton>(R.id.galleryButton)
        galleryBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "video/*"
            startActivityForResult(intent, galleryRequestCode)
        }

        val txt2speechButton = findViewById<ImageButton>(R.id.out_voice_button)
        txt2speechButton.setOnClickListener {

            tts.speak((txtTranslated.text), TextToSpeech.QUEUE_FLUSH, null, null)
        }


        videoView = findViewById(R.id.record_videoView)
        // now set up media controller for the play pause next pre
        val mediaCollection = MediaController(this)
        mediaCollection.setAnchorView(videoView)
        videoView.setMediaController(mediaCollection)




        initializePython()
    }
    private fun initializePython() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        pythonModule = py.getModule("client")
    }


    override fun onDestroy() {
        // Shut down TextToSpeech when the activity is destroyed
        tts.stop()
        tts.shutdown()
        super.onDestroy()
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


    private fun dispatchTakeVideoIntent() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takeVideoIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var videoFile: File? = null
            try {
                videoFile = createVideoFile()
            } catch (ex: Exception) {
                // handle exception
            }
            if (videoFile != null) {
                val videoURI = FileProvider.getUriForFile(
                    this,
                    "com.example.android.provider2",
                    videoFile
                )
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI)
                cameraVideo = videoFile
                startActivityForResult(takeVideoIntent, cameraRequestCode)
            }
        }
    }

    private fun createVideoFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile(
            "VID_${timeStamp}_",
            ".mp4",
            storageDir
        )
    }
}