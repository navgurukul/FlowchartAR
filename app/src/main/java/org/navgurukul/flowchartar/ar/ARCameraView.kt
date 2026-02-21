package org.navgurukul.flowchartar.ar

import android.content.Context
import android.view.SurfaceView
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException

class ARCameraView(context: Context) : SurfaceView(context) {
    private var session: Session? = null
    private var isSessionStarted = false
    
    fun setupSession() {
        try {
            session = Session(context)
            val config = Config(session).apply {
                updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                focusMode = Config.FocusMode.AUTO
            }
            session?.configure(config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun resumeSession() {
        try {
            session?.resume()
            isSessionStarted = true
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
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
    
    fun getFrame(): Frame? {
        return try {
            session?.update()
        } catch (e: Exception) {
            null
        }
    }
    
    fun isRunning() = isSessionStarted
}
