package com.example.anatronics

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

class NeutralFrontalCameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private lateinit var guidanceText: TextView

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.captureButton)
        guidanceText = findViewById(R.id.guidanceText)

        captureButton.isEnabled = false

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            startCamera()
        }

        captureButton.setOnClickListener {
            // TODO: capture the photo here
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val faceDetectorOptions = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build()
            val faceDetector = FaceDetection.getClient(faceDetectorOptions)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            faceDetector.process(image)
                                .addOnSuccessListener { faces ->
                                    if (faces.isNotEmpty()) {
                                        val face = faces[0]

                                        val yaw = face.headEulerAngleY
                                        val pitch = face.headEulerAngleX
                                        val roll = face.headEulerAngleZ

                                        if (yaw in -15f..15f && pitch in -10f..10f) {
                                            if (isNeutralFace(face)) {
                                                guidanceText.text = "Good! Frontal and neutral"
                                                captureButton.isEnabled = true
                                            } else {
                                                guidanceText.text = "Please keep a neutral expression"
                                                captureButton.isEnabled = false
                                            }
                                        } else {
                                            guidanceText.text = "Please face forward"
                                            captureButton.isEnabled = false
                                        }
                                    } else {
                                        guidanceText.text = "No face detected"
                                        captureButton.isEnabled = false
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun isNeutralFace(face: Face): Boolean {
        val leftMouth = face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position
        val rightMouth = face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position
        val bottomMouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position

        if (leftMouth == null || rightMouth == null || bottomMouth == null) return false

        // Calculate the vertical height of the mouth
        val mouthHeight = bottomMouth.y - (leftMouth.y + rightMouth.y) / 2
        // Calculate the width of the mouth
        val mouthWidth = rightMouth.x - leftMouth.x

        // Simple smile detection: smile usually has mouthHeight / mouthWidth > threshold
        val smileRatio = mouthHeight / mouthWidth

        // Adjust threshold experimentally; lower ratio = more neutral
        return smileRatio < 0.25
    }
}