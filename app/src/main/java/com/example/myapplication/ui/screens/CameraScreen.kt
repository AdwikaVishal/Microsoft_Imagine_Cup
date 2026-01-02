package com.example.myapplication.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.model.ScanUiState
import com.example.myapplication.utils.ObjectDetectorHelper
import com.example.myapplication.viewmodel.ScanViewModel
import com.example.myapplication.viewmodel.ScanViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Real-time camera screen for detecting Exits and scanning surroundings with Azure Vision.
 * Enhanced for Deaf users with Pictorial Guidance.
 * 
 * FEATURES:
 * - CameraX live preview
 * - Scan button to capture and analyze image with Azure Computer Vision
 * - Real-time exit detection for accessibility
 * - Result display with detected tags and objects
 * - Loading and error states with user-friendly messages
 * 
 * @property profile Ability profile for accessibility features (DEAF, BLIND, etc.)
 * @property onExitDetected Callback when an exit is detected
 * @property onNavigateBack Callback to navigate back
 * @property viewModel Optional ScanViewModel for Azure Vision integration
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CameraScreen(
    profile: AbilityProfile,
    onExitDetected: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: ScanViewModel? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val objectDetector = remember { ObjectDetectorHelper(context) }
    var detectedText by remember { mutableStateOf("Scanning...") }

    // Simulating detection of hazards/paths for Deaf users
    var showArrow by remember { mutableStateOf(false) }
    var showWarning by remember { mutableStateOf(false) }

    // Camera permission handling
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Image capture for Azure Vision
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    
    // Collect ViewModel state if available
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf<ScanUiState?>(null) }
    val scanResult by viewModel?.scanResult?.collectAsState() ?: remember { mutableStateOf<com.example.myapplication.model.ScanResult?>(null) }
    val isConfigured by viewModel?.isConfigured?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }

    // Handle captured image
    fun captureAndScan() {
        imageCapture?.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmap()
                    image.close()
                    viewModel?.scanImage(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraScreen", "Image capture failed: ${exception.message}")
                    // Fall back to using the last frame from image analysis if available
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Area") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !cameraPermissionState.status.isGranted -> {
                    // Permission not granted - show request UI
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Camera Permission Required",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() }
                        ) {
                            Text("Grant Permission")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onNavigateBack
                        ) {
                            Text("Go Back", color = Color.White)
                        }
                    }
                }
                else -> {
                    // Permission granted - show camera with controls
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Camera Preview
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    // Image capture for Azure Vision
                                    imageCapture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()

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
                                            imageCapture,
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
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // FEATURE: DEAF USER SUPPORT (PICTORIAL)
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
                                    Text(
                                        "EXIT",
                                        color = Color.Green,
                                        fontSize = 32.sp,
                                        modifier = Modifier.background(Color.Black.copy(alpha = 0.7f))
                                    )
                                }
                            }

                            if (showWarning) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 80.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Danger",
                                        tint = Color.Red,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Text(
                                        "HAZARD",
                                        color = Color.Red,
                                        fontSize = 32.sp,
                                        modifier = Modifier.background(Color.Black.copy(alpha = 0.7f))
                                    )
                                }
                            }
                        }

                        // Scan Button (FAB style) - Only show if ViewModel is provided
                        if (viewModel != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 100.dp)
                            ) {
                                ScanButton(
                                    isLoading = uiState == ScanUiState.Loading,
                                    isConfigured = isConfigured ?: false,
                                    onClick = { captureAndScan() }
                                )
                            }

                            // Result Overlay
                            AnimatedVisibility(
                                visible = uiState is ScanUiState.Success,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier.align(Alignment.TopCenter)
                            ) {
                                (uiState as? ScanUiState.Success)?.let { successState ->
                                    ScanResultOverlay(
                                        result = successState.result,
                                        onDismiss = { viewModel.resetState() },
                                        onRetry = { captureAndScan() }
                                    )
                                }
                            }

                            // Error Overlay
                            AnimatedVisibility(
                                visible = uiState is ScanUiState.Error ||
                                         uiState is ScanUiState.NotConfigured,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                val errorState = uiState
                                if (errorState is ScanUiState.Error) {
                                    ErrorOverlay(
                                        message = errorState.message,
                                        onDismiss = { viewModel.clearError() },
                                        onRetry = { captureAndScan() }
                                    )
                                } else if (errorState is ScanUiState.NotConfigured) {
                                    NotConfiguredOverlay(
                                        message = "Azure Vision is not configured yet.",
                                        onDismiss = { viewModel.resetState() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Scan button component with loading state
 * 
 * @property isLoading Whether the scan is in progress
 * @property isConfigured Whether Azure is configured
 * @property onClick Callback when button is clicked
 */
@Composable
fun ScanButton(
    isLoading: Boolean,
    isConfigured: Boolean,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = { if (!isLoading && isConfigured) onClick() },
        modifier = Modifier.size(72.dp),
        containerColor = if (isConfigured) Color(0xFF6750A4) else Color.Gray,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Scan Area",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Overlay showing scan results
 * 
 * @property result The scan result to display
 * @property onDismiss Callback to dismiss the overlay
 * @property onRetry Callback to retry the scan
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanResultOverlay(
    result: com.example.myapplication.model.ScanResult,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan Results",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description if available
            if (result.description.isNotBlank()) {
                Text(
                    text = "\"${result.description}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tags Section
            if (result.tags.isNotEmpty()) {
                Text(
                    text = "Detected Tags",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    result.tags.forEach { tag ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(tag, color = Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF6750A4).copy(alpha = 0.7f)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Objects Section
            if (result.objects.isNotEmpty()) {
                Text(
                    text = "Detected Objects",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(result.objects) { obj ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = obj.name,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${(obj.confidence * 100).toInt()}%",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Confidence indicator
            if (result.confidence > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { result.confidence },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF6750A4),
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
                Text(
                    text = "Confidence: ${(result.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
 * Error overlay for displaying user-friendly error messages
 * 
 * @property message The error message to display
 * @property onDismiss Callback to dismiss the error
 * @property onRetry Callback to retry the operation
 */
@Composable
fun ErrorOverlay(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = Color(0xFFB3261E),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Scan Failed",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Dismiss")
                }
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

/**
 * Overlay shown when Azure Vision is not configured
 * 
 * @property message Configuration message
 * @property onDismiss Callback to dismiss
 */
@Composable
fun NotConfiguredOverlay(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Not Configured",
                tint = Color.Yellow,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Azure Vision Not Configured",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue without Scan")
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

/**
 * Extension function to convert ImageProxy to Bitmap
 */
private fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
}

