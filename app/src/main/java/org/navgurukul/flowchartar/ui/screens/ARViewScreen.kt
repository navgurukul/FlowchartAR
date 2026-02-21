package org.navgurukul.flowchartar.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import org.navgurukul.flowchartar.ar.ImageRecognizer
import org.navgurukul.flowchartar.ar.Shape3DRenderer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARViewScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var detectedShapes by remember { mutableStateOf<List<org.navgurukul.flowchartar.ar.DetectedShape>>(emptyList()) }
    var isScanning by remember { mutableStateOf(true) }
    
    val imageRecognizer = remember { ImageRecognizer() }
    val shapeRenderer = remember { Shape3DRenderer() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "AR Scanner",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                // Camera Preview
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                                        if (isScanning) {
                                            processImage(imageProxy, imageRecognizer) { shapes ->
                                                detectedShapes = shapes
                                            }
                                        }
                                        imageProxy.close()
                                    }
                                }
                            
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("ARViewScreen", "Camera binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay with detected shapes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvas = drawContext.canvas.nativeCanvas
                    val width = size.width
                    val height = size.height
                    
                    detectedShapes.forEach { detected ->
                        val x = detected.x * width
                        val y = detected.y * height
                        val shapeSize = detected.width * width * 0.5f
                        
                        shapeRenderer.drawShape(
                            canvas,
                            detected.type,
                            x,
                            y,
                            shapeSize.coerceAtLeast(80f)
                        )
                    }
                }
                
                // Info overlay at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF6366F1).copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (detectedShapes.isEmpty()) 
                                    "Point camera at a flowchart" 
                                else 
                                    "Detected ${detectedShapes.size} shape${if (detectedShapes.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            
                            if (detectedShapes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = detectedShapes.joinToString(", ") { it.type.displayName },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                // Scanning indicator
                if (detectedShapes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(250.dp)
                    ) {
                        ScanningFrame()
                    }
                }
                
            } else {
                // Permission not granted
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please grant camera permission to scan flowcharts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        )
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}

@Composable
fun ScanningFrame() {
    var animationProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            animationProgress = (animationProgress + 0.02f) % 1f
            delay(16)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvas = drawContext.canvas.nativeCanvas
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 8f
        }
        
        val cornerLength = 60f
        val left = 0f
        val top = 0f
        val right = size.width
        val bottom = size.height
        
        // Top-left corner
        canvas.drawLine(left, top, left + cornerLength, top, paint)
        canvas.drawLine(left, top, left, top + cornerLength, paint)
        
        // Top-right corner
        canvas.drawLine(right - cornerLength, top, right, top, paint)
        canvas.drawLine(right, top, right, top + cornerLength, paint)
        
        // Bottom-left corner
        canvas.drawLine(left, bottom - cornerLength, left, bottom, paint)
        canvas.drawLine(left, bottom, left + cornerLength, bottom, paint)
        
        // Bottom-right corner
        canvas.drawLine(right - cornerLength, bottom, right, bottom, paint)
        canvas.drawLine(right, bottom - cornerLength, right, bottom, paint)
        
        // Scanning line
        val scanY = top + (bottom - top) * animationProgress
        paint.alpha = 150
        canvas.drawLine(left, scanY, right, scanY, paint)
    }
}

private fun processImage(
    imageProxy: ImageProxy,
    recognizer: ImageRecognizer,
    onShapesDetected: (List<org.navgurukul.flowchartar.ar.DetectedShape>) -> Unit
) {
    try {
        val bitmap = imageProxy.toBitmap()
        val shapes = recognizer.detectShapes(bitmap)
        onShapesDetected(shapes)
    } catch (e: Exception) {
        Log.e("ARViewScreen", "Image processing failed", e)
    }
}
