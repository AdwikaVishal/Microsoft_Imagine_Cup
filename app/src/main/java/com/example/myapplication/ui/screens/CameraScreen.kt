package com.example.myapplication.ui.screens

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.utils.ObjectDetectorHelper
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Real-time camera screen for detecting Exits.
 * Enhanced for Deaf users with Pictorial Guidance.
 */
@Composable
fun CameraScreen(
    profile: AbilityProfile,
    onExitDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val objectDetector = remember { ObjectDetectorHelper(context) }
    var detectedText by remember { mutableStateOf("Scanning...") }
    
    // Simulating detection of hazards/paths for Deaf users
    var showArrow by remember { mutableStateOf(false) }
    var showWarning by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                processImageProxy(imageProxy, objectDetector) { result ->
                                    detectedText = result
                                    if (result.contains("EXIT", true)) {
                                        onExitDetected(result)
                                        showArrow = true
                                        showWarning = false
                                    } else {
                                        showArrow = false
                                        // Randomly simulate hazard for demo
                                        showWarning = (System.currentTimeMillis() % 10000) < 2000 
                                    }
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraScreen", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay for text detection
        Text(
            text = detectedText,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .background(Color.Black.copy(alpha = 0.5f))
        )
        
        // FEATURE 2: DEAF USER SUPPORT (PICTORIAL)
        if (profile == AbilityProfile.DEAF) {
            if (showArrow) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go Forward",
                        tint = Color.Green,
                        modifier = Modifier.size(100.dp)
                    )
                    Text("EXIT", color = Color.Green, fontSize = 32.sp, modifier = Modifier.background(Color.Black))
                }
            }
            
            if (showWarning) {
                 Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Danger",
                        tint = Color.Red,
                        modifier = Modifier.size(80.dp)
                    )
                    Text("HAZARD", color = Color.Red, fontSize = 32.sp, modifier = Modifier.background(Color.Black))
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class) 
private fun processImageProxy(
    imageProxy: ImageProxy,
    detector: ObjectDetectorHelper,
    onResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.processImage(image, onResult)
        imageProxy.close()
    } else {
        imageProxy.close()
    }
}
