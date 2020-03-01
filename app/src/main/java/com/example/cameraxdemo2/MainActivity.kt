package com.example.cameraxdemo2

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CAMERA = 1000
    }

    private lateinit var viewFinder:PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewFinder = findViewById(R.id.view_finder)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")

        if (!hasCameraPermission()) {
            Log.d(TAG, "onResume: request camera permission")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        } else {
            // viewFiner.display may be null,
            // wait for the views to be properly laid out,
            viewFinder.post {
                bindCameraUseCases()
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA && grantResults.size == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onRequestPermissionsResult: permission camera PERMISSION_GRANTED")
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun bindCameraUseCases() {
        Log.d(TAG, "bindCameraUseCases: ")

        val cameraSelector = CameraSelector
                .Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val rotation = viewFinder.display.rotation
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview
                    .Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .setTargetRotation(rotation)
                    .build()
            preview.setSurfaceProvider(viewFinder.previewSurfaceProvider)

            cameraProvider.unbindAll()
            try {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
}