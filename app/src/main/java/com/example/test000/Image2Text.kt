package com.example.test000


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.*
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.example.test000.databinding.ActivityImage2textBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Image2Text : AppCompatActivity() {

    private lateinit var binding: ActivityImage2textBinding
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImage2textBinding.inflate(layoutInflater,null,false)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()
        requestPermission()


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

    }

    private fun requestPermission() {
        requestCameraPermissionIfMissing{ granted ->
            if (granted)
                startCamera()
            else
                Toast.makeText(this,"Please Allow the Permission",Toast.LENGTH_LONG).show()
        }

    }

    private fun requestCameraPermissionIfMissing(onResult: ((Boolean)-> Unit)) {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            onResult(true)
        else
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                onResult(it)
            }.launch((android.Manifest.permission.CAMERA))
    }

    private fun startCamera() {
        val processCameraProvider = ProcessCameraProvider.getInstance(this)
        processCameraProvider.addListener({
            try {
                val cameraProvider = processCameraProvider.get()
                val previewUseCase = buildPreviewUseCase()
                val imageCaptureUseCase = buildImageCaptureUseCase()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA,previewUseCase,imageCaptureUseCase)
            }catch (e: Exception){
                Log.d("Error",e.message.toString())
                //Toast.makeText(this,"Error starting the Camera",Toast.LENGTH_LONG).show()
            }
        },ContextCompat.getMainExecutor(this))
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        return ImageCapture.Builder().
        setFlashMode(FLASH_MODE_AUTO).
            build()
    }

    fun buildPreviewUseCase(): Preview{
        return Preview.Builder().build().also { it.setSurfaceProvider (binding.cameraPreview.surfaceProvider)}

    }
}