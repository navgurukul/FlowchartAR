package org.navgurukul.flowchartar.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.core.exceptions.UnavailableException
import kotlinx.coroutines.delay
import org.navgurukul.flowchartar.ar.ARCameraView
import org.navgurukul.flowchartar.ar.ImageRecognizer
import org.navgurukul.flowchartar.ar.Shape3DRenderer
import org.navgurukul.flowchartar.model.FlowchartShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARViewScreen(
    shape: FlowchartShape,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var arView by remember { mutableStateOf<ARCameraView?>(null) }
    var detectedShapes by remember { mutableStateOf<List<org.navgurukul.flowchartar.ar.DetectedShape>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val imageRecognizer = remember { ImageRecognizer() }
    val shapeRenderer = remember { Shape3DRenderer() }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            errorMessage = "Camera permission is required for AR"
        }
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission && arView != null) {
            try {
                arView?.setupSession()
                arView?.resumeSession()
                isScanning = true
                errorMessage = null
            } catch (e: UnavailableException) {
                errorMessage = "ARCore not available. Please install ARCore from Play Store."
            } catch (e: Exception) {
                errorMessage = "Failed to start AR: ${e.message}"
            }
        }
    }
    
    // Continuous scanning
    LaunchedEffect(isScanning) {
        while (isScanning && arView != null) {
            try {
                val frame = arView?.getFrame()
                frame?.let {
                    try {
                        val image = it.acquireCameraImage()
                        val bitmap = convertYuvToBitmap(image)
                        image.close()
                        
                        detectedShapes = imageRecognizer.detectShapes(bitmap)
                    } catch (e: Exception) {
                        // Continue scanning even if one frame fails
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Scanning error: ${e.message}"
            }
            delay(500) // Scan every 500ms
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            arView?.pauseSession()
            arView?.destroySession()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission) {
                // AR Camera View
                AndroidView(
                    factory = { ctx ->
                        ARCameraView(ctx).also { arView = it }
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
                            shapeSize
                        )
                    }
                }
                
                // Info overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Point camera at flowchart",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Detected: ${detectedShapes.size} shapes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please grant camera permission to use AR features",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text("Grant Permission")
                    }
                }
            }
            
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

private fun convertYuvToBitmap(image: android.media.Image): Bitmap {
    // Simple conversion - in production use more efficient method
    val width = image.width
    val height = image.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    
    // For now, create a placeholder bitmap
    // In production, properly convert YUV to RGB
    return bitmap
}
