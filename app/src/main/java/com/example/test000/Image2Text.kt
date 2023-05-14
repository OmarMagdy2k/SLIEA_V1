package com.example.test000


import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.ImageCapture.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date


class Image2Text : AppCompatActivity() {

    private var cameraRequestCode : Int = 123
    private var galleryRequestCode : Int = 122
    private lateinit var currentPhotoPath : String
    private lateinit var selectedImage : ImageView
    private lateinit var imageUris : MutableList<Uri>
    private lateinit var txtTranslated : TextView
    private lateinit var tts: TextToSpeech
    private lateinit var prediction : String
    private var translationLanguage: String = "En" // default to "en" if intent extra is not available


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image2text)


        // Check if the intent contains an extra with key "translationLanguage"
        if (intent.hasExtra("translationLanguage")) {
            translationLanguage = intent.getStringExtra("translationLanguage")!!
        }

        tts = TextToSpeech(applicationContext) { status ->
            if (status != TextToSpeech.ERROR) {
                if (translationLanguage == "En") {
                    // Set language for TextToSpeech
                    tts.language = Locale.US
                }
                else if (translationLanguage == "Ar"){
                    tts.language = Locale("ar")
                }
            }
        }

        txtTranslated = findViewById(R.id.out_trans_text)
        selectedImage = findViewById(R.id.imageView)

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
        val cameraBtn = findViewById<ImageButton>(R.id.cameraButton)
        cameraBtn.setOnClickListener {
            askCameraPermissions()
        }
        val galleryBtn = findViewById<ImageButton>(R.id.galleryButton)
        galleryBtn.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // New Code For Multiple images
            gallery.type = "image/*"
            gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            ////////////////////////
            startActivityForResult(gallery, galleryRequestCode)
        }

        val txt2speechButton = findViewById<ImageButton>(R.id.out_voice_button)
        txt2speechButton.setOnClickListener {

            tts.speak((txtTranslated.text), TextToSpeech.QUEUE_FLUSH, null, null)
        }

        val transBtn = findViewById<Button>(R.id.trans_button)
        transBtn.setOnClickListener {
            uploadImage(selectedImage)
        }
    }
    override fun onDestroy() {
        // Shut down TextToSpeech when the activity is destroyed
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    private fun uploadImage(imageView: ImageView) {
        imageView.drawable?.let { drawable ->
            val bitmap = (drawable as BitmapDrawable).bitmap
            val tempFile = File.createTempFile("image", ".jpg")
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/${tempFile.name}")

            val uploadTask = imageRef.putFile(Uri.fromFile(tempFile))

            uploadTask.addOnSuccessListener { taskSnapshot ->
                // File uploaded successfully, get the download URL
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                    val fileName = tempFile.name
                    translateImage(fileName)
                    selectedImage.setImageBitmap(bitmap) // set the bitmap to selectedImage
                    //Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                // Handle the failure event here
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun translateImage(fileName: String) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module: PyObject = py.getModule("client")
        try {
            if (translationLanguage == "En") {
                prediction = module.callAttr("translate_image_En", fileName)
                    .toJava(String::class.java)
            } else if (translationLanguage == "Ar") {
                prediction = module.callAttr("translate_image_Ar", fileName)
                    .toJava(String::class.java)
            }
            txtTranslated.post { // using txtTranslated.post to update TextView in UI thread
                txtTranslated.text = prediction
            }
        } catch (e: PyException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraRequestCode)
        } else {
            dispatchTakePictureIntent()
        }
    }
     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         if (requestCode == cameraRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            val f = File(currentPhotoPath)
            selectedImage.setImageURI(Uri.fromFile(f))

            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
            this.sendBroadcast(mediaScanIntent)
        }
        if (requestCode == galleryRequestCode && resultCode == RESULT_OK) {
            val contentUri = data?.data
            // New Code For Multiple images
            val clipData = data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    imageUris.add(uri)
                }
            } else {
                val uri = data?.data
                if (uri != null) {
                    imageUris.add(uri)
                }
            }
            ////////////////////////////////
//            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//            val imageFileName = "JPEG_${timeStamp}.${getFileExt(contentUri)}"
            selectedImage.setImageURI(contentUri)
        }
    }
//    private fun getFileExt(contentUri: Uri?): String {
//        val c: ContentResolver = contentResolver
//        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
//        return mime.getExtensionFromMimeType(contentUri?.let { c.getType(it) }).toString()
//    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: Exception) {
                // handle exception
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "com.example.android.provider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, cameraRequestCode)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}