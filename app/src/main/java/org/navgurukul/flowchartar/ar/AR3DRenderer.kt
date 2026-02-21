package org.navgurukul.flowchartar.ar

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import org.navgurukul.flowchartar.model.ShapeType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AR3DRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var session: Session? = null
    private val backgroundRenderer = BackgroundRenderer()
    private val virtualObjects = mutableListOf<VirtualObject>()
    
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    
    var onShapesDetected: ((List<DetectedShape>) -> Unit)? = null
    
    fun setupSession() {
        try {
            session = Session(context)
            val config = Config(session).apply {
                updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                focusMode = Config.FocusMode.AUTO
                lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            }
            session?.configure(config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun resumeSession() {
        try {
            session?.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
        }
    }
    
    fun pauseSession() {
        session?.pause()
    }
    
    fun destroySession() {
        session?.close()
        session = null
    }
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        
        try {
            backgroundRenderer.createOnGlThread(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        session?.setDisplayGeometry(0, width, height)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        val session = this.session ?: return
        
        try {
            session.setCameraTextureName(backgroundRenderer.textureId)
            val frame = session.update()
            val camera = frame.camera
            
            if (camera.trackingState == TrackingState.TRACKING) {
                // Draw background
                backgroundRenderer.draw(frame)
                
                // Get projection and view matrices
                camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)
                camera.getViewMatrix(viewMatrix, 0)
                
                // Enable depth testing for 3D
                GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                
                // Detect planes and place objects
                val detectedShapes = mutableListOf<DetectedShape>()
                
                for (plane in frame.getUpdatedTrackables(Plane::class.java)) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        val centerPose = plane.centerPose
                        
                        // Classify plane and create virtual object
                        val shapeType = classifyPlaneToShape(plane)
                        
                        // Add virtual object if not already added
                        if (virtualObjects.none { it.anchor == plane.centerPose }) {
                            val anchor = session.createAnchor(centerPose)
                            val virtualObject = VirtualObject(
                                shapeType = shapeType,
                                anchor = centerPose,
                                scale = 0.2f
                            )
                            virtualObjects.add(virtualObject)
                            
                            detectedShapes.add(
                                DetectedShape(
                                    type = shapeType,
                                    x = 0.5f,
                                    y = 0.5f,
                                    width = 0.2f,
                                    height = 0.2f
                                )
                            )
                        }
                    }
                }
                
                // Draw all virtual objects with shadows
                val lightIntensity = frame.lightEstimate.pixelIntensity
                
                virtualObjects.forEach { obj ->
                    drawVirtualObject(obj, viewMatrix, projectionMatrix, lightIntensity)
                }
                
                onShapesDetected?.invoke(detectedShapes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun drawVirtualObject(
        obj: VirtualObject,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        lightIntensity: Float
    ) {
        val modelMatrix = FloatArray(16)
        obj.anchor.toMatrix(modelMatrix, 0)
        
        // Apply scale
        Matrix.scaleM(modelMatrix, 0, obj.scale, obj.scale, obj.scale)
        
        // Draw shadow first (on the ground plane)
        drawShadow(modelMatrix, viewMatrix, projectionMatrix)
        
        // Draw the 3D shape
        when (obj.shapeType) {
            ShapeType.START_END -> drawOval3D(modelMatrix, viewMatrix, projectionMatrix, obj.shapeType, lightIntensity)
            ShapeType.PROCESS -> drawCube3D(modelMatrix, viewMatrix, projectionMatrix, obj.shapeType, lightIntensity)
            ShapeType.DECISION -> drawDiamond3D(modelMatrix, viewMatrix, projectionMatrix, obj.shapeType, lightIntensity)
            ShapeType.INPUT_OUTPUT -> drawParallelogram3D(modelMatrix, viewMatrix, projectionMatrix, obj.shapeType, lightIntensity)
            ShapeType.CONNECTOR -> drawSphere3D(modelMatrix, viewMatrix, projectionMatrix, obj.shapeType, lightIntensity)
        }
    }
    
    private fun drawShadow(
        modelMatrix: FloatArray,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) {
        val shadowMatrix = FloatArray(16)
        Matrix.multiplyMM(shadowMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        // Flatten to ground (y = 0)
        shadowMatrix[5] = 0.01f // Very small y scale for shadow
        shadowMatrix[13] = 0f // y position = 0
        
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, shadowMatrix, 0)
        
        // Draw dark semi-transparent quad for shadow
        drawQuad(mvpMatrix, floatArrayOf(0f, 0f, 0f, 0.5f))
    }
    
    private fun drawCube3D(
        modelMatrix: FloatArray,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        shapeType: ShapeType,
        lightIntensity: Float
    ) {
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        val color = getColorArray(shapeType, lightIntensity)
        
        // Draw 6 faces of cube with lighting
        drawCubeFaces(mvpMatrix, color)
    }
    
    private fun drawDiamond3D(
        modelMatrix: FloatArray,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        shapeType: ShapeType,
        lightIntensity: Float
    ) {
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        val color = getColorArray(shapeType, lightIntensity)
        
        // Draw diamond as pyramid + inverted pyramid
        drawPyramid(mvpMatrix, color, false)
        drawPyramid(mvpMatrix, color, true)
    }
    
    private fun drawOval3D(
        modelMatrix: FloatArray,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        shapeType: ShapeType,
        lightIntensity: Float
    ) {
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        val color = getColorArray(shapeType, lightIntensity)
        
        // Draw ellipsoid
        drawEllipsoid(mvpMatrix, color)
    }
    
    private fun drawParallelogram3D(
        modelMatrix: FloatArray,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        shapeType: ShapeType,
        lightIntensity: Float
    ) {
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        val color = getColorArray(shapeType, lightIntensity)
        
        // Draw skewed box
        drawSkewedBox(mvpMatrix, color)
    }
    
    private fun drawSphere3D(
        modelMatrix: FloatArray,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        shapeType: ShapeType,
        lightIntensity: Float
    ) {
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        val color = getColorArray(shapeType, lightIntensity)
        
        // Draw sphere
        drawSphere(mvpMatrix, color)
    }
    
    private fun getColorArray(shapeType: ShapeType, lightIntensity: Float): FloatArray {
        val baseColor = shapeType.color
        return floatArrayOf(
            baseColor.red * lightIntensity,
            baseColor.green * lightIntensity,
            baseColor.blue * lightIntensity,
            0.9f
        )
    }
    
    private fun classifyPlaneToShape(plane: Plane): ShapeType {
        // Simple classification based on plane properties
        val extentX = plane.extentX
        val extentZ = plane.extentZ
        val ratio = extentX / extentZ
        
        return when {
            ratio > 1.5f -> ShapeType.PROCESS
            ratio < 0.7f -> ShapeType.INPUT_OUTPUT
            plane.type == Plane.Type.VERTICAL -> ShapeType.DECISION
            else -> ShapeType.START_END
        }
    }
    
    // Placeholder drawing methods - implement with actual OpenGL vertex buffers
    private fun drawQuad(mvpMatrix: FloatArray, color: FloatArray) {
        // TODO: Implement with vertex buffer
    }
    
    private fun drawCubeFaces(mvpMatrix: FloatArray, color: FloatArray) {
        // TODO: Implement with vertex buffer
    }
    
    private fun drawPyramid(mvpMatrix: FloatArray, color: FloatArray, inverted: Boolean) {
        // TODO: Implement with vertex buffer
    }
    
    private fun drawEllipsoid(mvpMatrix: FloatArray, color: FloatArray) {
        // TODO: Implement with vertex buffer
    }
    
    private fun drawSkewedBox(mvpMatrix: FloatArray, color: FloatArray) {
        // TODO: Implement with vertex buffer
    }
    
    private fun drawSphere(mvpMatrix: FloatArray, color: FloatArray) {
        // TODO: Implement with vertex buffer
    }
    
    data class VirtualObject(
        val shapeType: ShapeType,
        val anchor: Pose,
        val scale: Float
    )
}

class BackgroundRenderer {
    var textureId: Int = -1
        private set
    
    fun createOnGlThread(context: Context) {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    }
    
    fun draw(frame: Frame) {
        // Draw camera background
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        
        // TODO: Implement background rendering with camera texture
        
        GLES20.glDepthMask(true)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }
}
