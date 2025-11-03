package com.screencast.tv

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.screencast.tv.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var qrCodeImage: ImageView
    private lateinit var statusText: TextView
    private lateinit var ipText: TextView
    
    private val SERVER_PORT = 8888
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            
            qrCodeImage = findViewById(R.id.qrCodeImage)
            statusText = findViewById(R.id.statusText)
            ipText = findViewById(R.id.ipText)
            
            initializeApp()
        } catch (e: Exception) {
            e.printStackTrace()
            // 显示错误对话框
            android.app.AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("应用初始化失败: ${e.message}")
                .setPositiveButton("重试") { _, _ -> initializeApp() }
                .setNegativeButton("退出") { _, _ -> finish() }
                .show()
        }
    }
    
    private fun initializeApp() {
        try {
            // 获取本机IP地址
            val ipAddress = NetworkUtils.getLocalIPAddress(this)
            if (ipAddress != null) {
                ipText.text = "IP地址: $ipAddress:$SERVER_PORT"
                generateQRCode("$ipAddress:$SERVER_PORT")
                startVideoReceiverService()
            } else {
                statusText.text = "无法获取IP地址，请检查网络连接"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            statusText.text = "初始化失败: ${e.message}"
        }
    }
    
    private fun generateQRCode(content: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("QRCode", "开始生成二维码: $content")
                
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                android.util.Log.d("QRCode", "二维码尺寸: ${width}x${height}")
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                
                android.util.Log.d("QRCode", "二维码生成完成，准备显示")
                
                runOnUiThread {
                    try {
                        qrCodeImage.setImageBitmap(bitmap)
                        android.util.Log.d("QRCode", "二维码显示成功")
                        statusText.text = "二维码已生成"
                    } catch (e: Exception) {
                        android.util.Log.e("QRCode", "显示二维码失败", e)
                        statusText.text = "二维码显示失败: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("QRCode", "生成二维码失败", e)
                runOnUiThread {
                    statusText.text = "生成二维码失败: ${e.message}"
                }
                e.printStackTrace()
            }
        }
    }
    
    private fun startVideoReceiverService() {
        val intent = Intent(this, com.screencast.tv.service.VideoReceiverService::class.java)
        intent.putExtra("port", SERVER_PORT)
        startService(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, com.screencast.tv.service.VideoReceiverService::class.java)
        stopService(intent)
    }
}
