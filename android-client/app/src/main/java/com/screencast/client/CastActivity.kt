package com.screencast.client

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.screencast.client.service.ScreenCastService

class CastActivity : AppCompatActivity() {
    
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    
    private var serverIp: String = ""
    private var serverPort: Int = 0
    private var isCasting = false
    
    private val REQUEST_MEDIA_PROJECTION = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cast)
        
        statusText = findViewById(R.id.statusText)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        
        serverIp = intent.getStringExtra("server_ip") ?: ""
        serverPort = intent.getIntExtra("server_port", 0)
        
        statusText.text = "服务器: $serverIp:$serverPort"
        
        startButton.setOnClickListener {
            requestScreenCapture()
        }
        
        stopButton.setOnClickListener {
            stopCasting()
        }
        
        updateButtonStates()
    }
    
    private fun requestScreenCapture() {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                startCasting(resultCode, data)
            } else {
                Toast.makeText(this, "需要屏幕录制权限", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startCasting(resultCode: Int, data: Intent) {
        val intent = Intent(this, ScreenCastService::class.java)
        intent.putExtra("resultCode", resultCode)
        intent.putExtra("data", data)
        intent.putExtra("server_ip", serverIp)
        intent.putExtra("server_port", serverPort)
        startForegroundService(intent)
        
        isCasting = true
        statusText.text = "投屏中... ($serverIp:$serverPort)"
        updateButtonStates()
    }
    
    private fun stopCasting() {
        val intent = Intent(this, ScreenCastService::class.java)
        stopService(intent)
        
        isCasting = false
        statusText.text = "已停止投屏"
        updateButtonStates()
    }
    
    private fun updateButtonStates() {
        startButton.isEnabled = !isCasting
        stopButton.isEnabled = isCasting
    }
}
