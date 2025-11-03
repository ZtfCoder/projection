package com.screencast.tv

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class DisplayActivity : AppCompatActivity() {
    
    private lateinit var surfaceView: SurfaceView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置全屏
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContentView(R.layout.activity_display)
        
        surfaceView = findViewById(R.id.surfaceView)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                // Surface创建完成，通知Service可以开始解码
                VideoDisplayManager.setSurface(holder.surface)
            }
            
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                VideoDisplayManager.setSurface(null)
            }
        })
    }
}

object VideoDisplayManager {
    private var surface: android.view.Surface? = null
    private var callback: ((android.view.Surface?) -> Unit)? = null
    
    fun setSurface(s: android.view.Surface?) {
        surface = s
        callback?.invoke(s)
    }
    
    fun getSurface() = surface
    
    fun setSurfaceCallback(cb: (android.view.Surface?) -> Unit) {
        callback = cb
    }
}
