package org.navgurukul.flowchartar.ar

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import org.navgurukul.flowchartar.model.ShapeType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ARRenderer(
    private val context: Context,
    private val onStatusUpdate: (planes: Int, objects: Int) -> Unit
) : GLSurfaceView.Renderer {
    
    private var session: Session? = null
    private val anchoredObjects = mutableListOf<AnchoredShape>()
    private var currentShapeIndex = 0
    
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    
    private var backgroundRenderer: BackgroundRenderer? = null
    private var shapeRenderer: ShapeRenderer? = null
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        try {
            backgroundRenderer = BackgroundRenderer()
            backgroundRenderer?.createOnGlThread(context)
            
            shapeRenderer = ShapeRenderer()
            shapeRenderer?.createOnGlThread()
            
            // Initialize ARCore session
            session = Session(context).apply {
                val config = Config(this).apply {
                    updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    focusMode = Config.FocusMode.AUTO
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                }
                configure(config)
            }
        } catch (e: Exception) {
            Log.e("ARRenderer", "Failed to create AR session", e)
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
            session.setCameraTextureName(backgroundRenderer?.textureId ?: 0)
            val frame = session.update()
            val camera = frame.camera
            
            if (camera.trackingState != TrackingState.TRACKING) {
                return
            }
            
            // Draw camera background
            backgroundRenderer?.draw(frame)
            
            // Get camera matrices
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100f)
            camera.getViewMatrix(viewMatrix, 0)
            
            // Count detected planes
            val planes = frame.getUpdatedTrackables(Plane::class.java)
                .count { it.trackingState == TrackingState.TRACKING }
            
            // Update status
            onStatusUpdate(planes, anchoredObjects.size)
            
            // Draw all anchored 3D shapes
            anchoredObjects.forEach { anchoredShape ->
                if (anchoredShape.anchor.trackingState == TrackingState.TRACKING) {
                    drawShape(anchoredShape, camera)
                }
            }
            
        } catch (e: CameraNotAvailableException) {
            Log.e("ARRenderer", "Camera not available", e)
        } catch (e: Exception) {
            Log.e("ARRenderer", "Error in onDrawFrame", e)
        }
    }
    
    private fun drawShape(anchoredShape: AnchoredShape, camera: Camera) {
        val modelMatrix = FloatArray(16)
        anchoredShape.anchor.pose.toMatrix(modelMatrix, 0)
        
        // Scale the shape
        Matrix.scaleM(modelMatrix, 0, 0.2f, 0.2f, 0.2f)
        
        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        // Draw the shape
        shapeRenderer?.draw(mvpMatrix, anchoredShape.shapeType)
    }
    
    fun handleTap(x: Float, y: Float) {
        val session = this.session ?: return
        
        try {
            val frame = session.update()
            
            // Hit test to find a plane
            val hits = frame.hitTest(x, y)
            
            for (hit in hits) {
                val trackable = hit.trackable
                
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    // Create anchor at hit location
                    val anchor = hit.createAnchor()
                    
                    // Cycle through shape types
                    val shapeType = ShapeType.values()[currentShapeIndex % ShapeType.values().size]
                    currentShapeIndex++
                    
                    // Add anchored shape
                    anchoredObjects.add(AnchoredShape(anchor, shapeType))
                    
                    break
                }
            }
        } catch (e: Exception) {
            Log.e("ARRenderer", "Error handling tap", e)
        }
    }
    
    fun onResume() {
        try {
            session?.resume()
        } catch (e: CameraNotAvailableException) {
            Log.e("ARRenderer", "Camera not available on resume", e)
        }
    }
    
    fun onPause() {
        session?.pause()
    }
    
    fun destroy() {
        session?.close()
        session = null
    }
    
    data class AnchoredShape(
        val anchor: Anchor,
        val shapeType: ShapeType
    )
}

class BackgroundRenderer {
    var textureId: Int = -1
        private set
    
    fun createOnGlThread(context: Context) {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    }
    
    fun draw(frame: Frame) {
        // Background is drawn automatically by ARCore
    }
}

class ShapeRenderer {
    private var program: Int = 0
    
    fun createOnGlThread() {
        val vertexShader = """
            uniform mat4 u_ModelViewProjection;
            attribute vec4 a_Position;
            attribute vec4 a_Color;
            varying vec4 v_Color;
            
            void main() {
                v_Color = a_Color;
                gl_Position = u_ModelViewProjection * a_Position;
            }
        """.trimIndent()
        
        val fragmentShader = """
            precision mediump float;
            varying vec4 v_Color;
            
            void main() {
                gl_FragColor = v_Color;
            }
        """.trimIndent()
        
        val vertexShaderId = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fragmentShaderId = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShaderId)
        GLES20.glAttachShader(program, fragmentShaderId)
        GLES20.glLinkProgram(program)
    }
    
    fun draw(mvpMatrix: FloatArray, shapeType: ShapeType) {
        GLES20.glUseProgram(program)
        
        val color = shapeType.color
        val colorArray = floatArrayOf(color.red, color.green, color.blue, 0.9f)
        
        // Simple cube vertices for now
        val vertices = getCubeVertices()
        
        val vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        vertexBuffer.position(0)
        
        val positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        
        val colorHandle = GLES20.glGetAttribLocation(program, "a_Color")
        GLES20.glVertexAttrib4fv(colorHandle, colorArray, 0)
        
        val mvpHandle = GLES20.glGetUniformLocation(program, "u_ModelViewProjection")
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 3)
        
        GLES20.glDisableVertexAttribArray(positionHandle)
    }
    
    private fun getCubeVertices(): FloatArray {
        return floatArrayOf(
            // Front face
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            
            // Back face
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            
            // Top face
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            
            // Bottom face
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            
            // Right face
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            
            // Left face
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f
        )
    }
    
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}

// Extension for OpenGL ES
object GLES11Ext {
    const val GL_TEXTURE_EXTERNAL_OES = 0x8D65
}
