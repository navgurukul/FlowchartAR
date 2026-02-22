package org.navgurukul.flowchartar.ar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException

class ARCoreSurfaceDetector(private val context: Context) {
    private var session: Session? = null
    private var isSessionStarted = false
    
    val detectedPlanes = mutableListOf<Plane>()
    var onPlanesDetected: ((List<Plane>) -> Unit)? = null
    
    fun setupSession(): Boolean {
        return try {
            if (!ArCoreApk.getInstance().checkAvailability(context).isSupported) {
                return false
            }
            
            session = Session(context).apply {
                val config = Config(this).apply {
                    updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    focusMode = Config.FocusMode.AUTO
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    depthMode = Config.DepthMode.AUTOMATIC
                }
                configure(config)
            }
            true
        } catch (e: UnavailableException) {
            e.printStackTrace()
            false
        }
    }
    
    fun resumeSession(): Boolean {
        return try {
            session?.resume()
            isSessionStarted = true
            true
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
            false
        }
    }
    
    fun pauseSession() {
        session?.pause()
        isSessionStarted = false
    }
    
    fun destroySession() {
        session?.close()
        session = null
    }
    
    fun updateFrame(): Frame? {
        return try {
            session?.update()
        } catch (e: Exception) {
            null
        }
    }
    
    fun processFrame(frame: Frame) {
        // Get all tracked planes
        val updatedPlanes = frame.getUpdatedTrackables(Plane::class.java)
            .filter { it.trackingState == TrackingState.TRACKING }
            .toList()
        
        detectedPlanes.clear()
        detectedPlanes.addAll(updatedPlanes)
        
        if (updatedPlanes.isNotEmpty()) {
            onPlanesDetected?.invoke(updatedPlanes)
        }
    }
    
    fun drawPlaneVisualization(
        canvas: Canvas,
        plane: Plane,
        viewWidth: Float,
        viewHeight: Float,
        projectionMatrix: FloatArray,
        viewMatrix: FloatArray
    ) {
        if (plane.trackingState != TrackingState.TRACKING) return
        
        val paint = Paint().apply {
            color = when (plane.type) {
                Plane.Type.HORIZONTAL_UPWARD_FACING -> android.graphics.Color.argb(100, 76, 175, 80)
                Plane.Type.HORIZONTAL_DOWNWARD_FACING -> android.graphics.Color.argb(100, 33, 150, 243)
                Plane.Type.VERTICAL -> android.graphics.Color.argb(100, 255, 193, 7)
                else -> android.graphics.Color.argb(100, 156, 39, 176)
            }
            style = Paint.Style.FILL
        }
        
        val outlinePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        
        // Draw plane polygon
        val polygon = plane.polygon
        if (polygon.limit() >= 6) {
            val path = Path()
            var first = true
            
            for (i in 0 until polygon.limit() / 2) {
                val x = polygon.get(i * 2)
                val z = polygon.get(i * 2 + 1)
                
                // Project 3D point to 2D screen coordinates
                val screenPoint = project3DTo2D(
                    x, 0f, z,
                    plane.centerPose,
                    projectionMatrix,
                    viewMatrix,
                    viewWidth,
                    viewHeight
                )
                
                if (first) {
                    path.moveTo(screenPoint[0], screenPoint[1])
                    first = false
                } else {
                    path.lineTo(screenPoint[0], screenPoint[1])
                }
            }
            path.close()
            
            canvas.drawPath(path, paint)
            canvas.drawPath(path, outlinePaint)
        }
        
        // Draw center point
        val centerPoint = project3DTo2D(
            0f, 0f, 0f,
            plane.centerPose,
            projectionMatrix,
            viewMatrix,
            viewWidth,
            viewHeight
        )
        
        val centerPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerPoint[0], centerPoint[1], 10f, centerPaint)
    }
    
    private fun project3DTo2D(
        x: Float,
        y: Float,
        z: Float,
        pose: Pose,
        projectionMatrix: FloatArray,
        viewMatrix: FloatArray,
        viewWidth: Float,
        viewHeight: Float
    ): FloatArray {
        // Transform point by pose
        val worldPos = FloatArray(4)
        val poseMatrix = FloatArray(16)
        pose.toMatrix(poseMatrix, 0)
        
        val localPos = floatArrayOf(x, y, z, 1f)
        android.opengl.Matrix.multiplyMV(worldPos, 0, poseMatrix, 0, localPos, 0)
        
        // Apply view matrix
        val viewPos = FloatArray(4)
        android.opengl.Matrix.multiplyMV(viewPos, 0, viewMatrix, 0, worldPos, 0)
        
        // Apply projection matrix
        val clipPos = FloatArray(4)
        android.opengl.Matrix.multiplyMV(clipPos, 0, projectionMatrix, 0, viewPos, 0)
        
        // Perspective divide
        val ndcX = clipPos[0] / clipPos[3]
        val ndcY = clipPos[1] / clipPos[3]
        
        // Convert to screen coordinates
        val screenX = (ndcX + 1f) * 0.5f * viewWidth
        val screenY = (1f - ndcY) * 0.5f * viewHeight
        
        return floatArrayOf(screenX, screenY)
    }
    
    fun getProjectionMatrix(near: Float = 0.1f, far: Float = 100f): FloatArray {
        val matrix = FloatArray(16)
        session?.let { session ->
            try {
                val frame = session.update()
                frame.camera.getProjectionMatrix(matrix, 0, near, far)
            } catch (e: Exception) {
                android.opengl.Matrix.setIdentityM(matrix, 0)
            }
        }
        return matrix
    }
    
    fun getViewMatrix(): FloatArray {
        val matrix = FloatArray(16)
        session?.let { session ->
            try {
                val frame = session.update()
                frame.camera.getViewMatrix(matrix, 0)
            } catch (e: Exception) {
                android.opengl.Matrix.setIdentityM(matrix, 0)
            }
        }
        return matrix
    }
    
    fun isRunning() = isSessionStarted
}
