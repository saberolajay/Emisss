package com.example.emis

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QrCodeActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var scanResultTextView: TextView
    private lateinit var markAttendanceButton: Button
    private lateinit var scanResultCard: CardView
    private lateinit var scanningLine: View
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val REQUEST_CAMERA_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        // Initialize UI components
        previewView = findViewById(R.id.viewFinder)
        scanResultTextView = findViewById(R.id.scanResult)
        markAttendanceButton = findViewById(R.id.btnMarkAttendance)
        scanResultCard = findViewById(R.id.scanResultCard)
        scanningLine = findViewById(R.id.scanningLine)
        val btnExit: ImageButton = findViewById(R.id.btnExit)

        // Animate scanning line
        animateScanningLine()

        // Exit button click handler
        btnExit.setOnClickListener { finish() }

        // Check camera permissions and initialize CameraX
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }

        // Handle "Mark Attendance" button click
        markAttendanceButton.setOnClickListener {
            Toast.makeText(this, "Attendance marked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider)
            } catch (e: Exception) {
                Log.e("QrCodeActivity", "Camera initialization failed: ${e.localizedMessage}")
                Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Animate the scanning line
    private fun animateScanningLine() {
        val animation = TranslateAnimation(0f, 0f, 0f, 500f)
        animation.duration = 2000
        animation.repeatCount = TranslateAnimation.INFINITE
        animation.repeatMode = TranslateAnimation.REVERSE
        scanningLine.startAnimation(animation)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        // Setup the Preview use case
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Setup Image Analysis use case
        val imageAnalysis = ImageAnalysis.Builder().build()
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val scanner: BarcodeScanner = BarcodeScanning.getClient()
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            handleScanResult(barcode)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("QrCodeActivity", "Scanning failed: ${e.message}")
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }

        // Select the camera (back camera)
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (e: Exception) {
            Log.e("QrCodeActivity", "Use case binding failed: ${e.localizedMessage}")
            Toast.makeText(this, "Failed to bind camera use cases", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleScanResult(barcode: Barcode) {
        val value = barcode.displayValue ?: "No value"
        scanResultTextView.text = "Scan Result: $value"
        scanResultCard.visibility = View.VISIBLE
    }

    // Handle runtime permission results
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
