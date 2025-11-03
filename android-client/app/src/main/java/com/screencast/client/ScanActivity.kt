package com.screencast.client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class ScanActivity : AppCompatActivity() {
    
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var capture: CaptureManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        
        barcodeView = findViewById(R.id.barcode_scanner)
        
        capture = CaptureManager(this, barcodeView)
        capture.initializeFromIntent(intent, savedInstanceState)
        
        barcodeView.decodeContinuous { result ->
            result?.let {
                // 解析二维码内容，格式：IP:PORT
                val content = it.text
                parseQRCodeAndConnect(content)
            }
        }
        
        capture.decode()
    }
    
    private fun parseQRCodeAndConnect(content: String) {
        try {
            val parts = content.split(":")
            if (parts.size == 2) {
                val ip = parts[0]
                val port = parts[1].toInt()
                
                // 跳转到投屏页面
                val intent = Intent(this, CastActivity::class.java)
                intent.putExtra("server_ip", ip)
                intent.putExtra("server_port", port)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "二维码格式错误", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "解析二维码失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        capture.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        capture.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }
}
