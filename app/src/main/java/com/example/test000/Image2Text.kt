package com.example.test000


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView


import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.*
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PackageManagerCompat
import com.example.test000.databinding.ActivityImage2textBinding
import java.io.File
import java.io.FileDescriptor
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


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
        val galleryBtn = findViewById<ImageButton>(R.id.galleryButton);
        galleryBtn.setOnClickListener {
            Toast.makeText(this,"Gallery Btn is Clicked.",Toast.LENGTH_LONG).show()
        }

       selectedImage = findViewById(R.id.imageView)
//        galleryBtn.setOnClickListener {
//            Toast.makeText(this,"Camera Btn is Clicked.",Toast.LENGTH_LONG).show()
//        }

    }

    private fun askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), cameraRequestCode)
        } else {
            openCamera()
        }
    }

     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         if (requestCode == cameraRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun openCamera() {
        val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(camera, cameraRequestCode)
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            val image = data?.extras?.get("data") as Bitmap?
            selectedImage.setImageBitmap(image)
        }
    }



    fun dispatchTakePictureIntent() {
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
                    "net.smallacademy.android.fileprovider",
                    photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, cameraRequestCode)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yy-MM-dd-HH-mm-ss-SSS").format(Date())
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




//
//    private fun startCamera() {
//        val processCameraProvider = ProcessCameraProvider.getInstance(this)
//        processCameraProvider.addListener({
//            try {
//                val cameraProvider = processCameraProvider.get()
//                val previewUseCase = buildPreviewUseCase()
//                val imageCaptureUseCase = buildImageCaptureUseCase()
//
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA,previewUseCase,imageCaptureUseCase)
//            }catch (e: Exception){
//                Log.d("Error",e.message.toString())
//                //Toast.makeText(this,"Error starting the Camera",Toast.LENGTH_LONG).show()
//            }
//        },ContextCompat.getMainExecutor(this))
//    }
//
//    private fun buildImageCaptureUseCase(): ImageCapture {
//        return ImageCapture.Builder().
//        setFlashMode(FLASH_MODE_AUTO).
//            build()
//    }
//
//    private fun buildPreviewUseCase(): Preview{
//        return Preview.Builder().build().also { it.setSurfaceProvider (binding.cameraPreview.surfaceProvider)}
//
//    }
}