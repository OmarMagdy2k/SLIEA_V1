package com.example.test000


import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.ImageCapture.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class Image2Text : AppCompatActivity() {


    private var cameraRequestCode : Int = 123
    private lateinit var currentPhotoPath : String
    private lateinit var selectedImage : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image2text)



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
            Toast.makeText(this,"Gallery Btn is Clicked.",Toast.LENGTH_LONG).show()
        }

       selectedImage = findViewById(R.id.imageView)

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

        }
    }



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
                val photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, cameraRequestCode)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_$timeStamp"
        // val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        currentPhotoPath = image.absolutePath
        return image

    }

}