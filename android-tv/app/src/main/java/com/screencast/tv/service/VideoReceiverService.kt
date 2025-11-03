package com.screencast.tv.service

import android.app.Service
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.IBinder
import android.view.Surface
import com.screencast.tv.DisplayActivity
import com.screencast.tv.VideoDisplayManager
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer

class VideoReceiverService : Service() {
    
    private var serverSocket: ServerSocket? = null
    private var udpSocket: DatagramSocket? = null
    private var clientSocket: Socket? = null
    private var mediaCodec: MediaCodec? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isReceiving = false
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val port = intent?.getIntExtra("port", 8888) ?: 8888
        
        serviceScope.launch {
            startServer(port)
        }
        
        return START_STICKY
    }
    
    private suspend fun startServer(port: Int) {
        try {
            // 启动TCP服务器等待连接
            serverSocket = ServerSocket(port)
            
            while (coroutineContext.isActive) {
                // 等待客户端连接
                clientSocket = serverSocket?.accept()
                
                // 读取控制信息
                val input = DataInputStream(clientSocket?.getInputStream())
                val message = input.readUTF()
                
                if (message == "READY") {
                    // 客户端准备好了，启动接收
                    isReceiving = true
                    
                    // 启动显示Activity
                    val displayIntent = Intent(this@VideoReceiverService, DisplayActivity::class.java)
                    displayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(displayIntent)
                    
                    // 启动UDP接收
                    startUDPReceiver(port + 1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun startUDPReceiver(port: Int) {
        withContext(Dispatchers.IO) {
            try {
                udpSocket = DatagramSocket(port)
                
                // 等待Surface准备好
                delay(1000)
                
                // 配置解码器
                setupMediaCodec()
                
                val buffer = ByteArray(65536)
                val packet = DatagramPacket(buffer, buffer.size)
                
                while (isReceiving && coroutineContext.isActive) {
                    udpSocket?.receive(packet)
                    
                    if (packet.length > 0) {
                        val data = packet.data.copyOfRange(0, packet.length)
                        decodeVideoData(data)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun setupMediaCodec() {
        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1280, 720)
            
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            
            // 等待Surface可用
            VideoDisplayManager.setSurfaceCallback { surface ->
                if (surface != null) {
                    try {
                        mediaCodec?.configure(format, surface, null, 0)
                        mediaCodec?.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            // 如果Surface已经可用
            VideoDisplayManager.getSurface()?.let { surface ->
                mediaCodec?.configure(format, surface, null, 0)
                mediaCodec?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun decodeVideoData(data: ByteArray) {
        try {
            val inputBufferId = mediaCodec?.dequeueInputBuffer(10000) ?: return
            
            if (inputBufferId >= 0) {
                val inputBuffer = mediaCodec?.getInputBuffer(inputBufferId)
                inputBuffer?.clear()
                inputBuffer?.put(data)
                
                mediaCodec?.queueInputBuffer(inputBufferId, 0, data.size, System.nanoTime() / 1000, 0)
            }
            
            val bufferInfo = MediaCodec.BufferInfo()
            val outputBufferId = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: return
            
            if (outputBufferId >= 0) {
                mediaCodec?.releaseOutputBuffer(outputBufferId, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        isReceiving = false
        serviceScope.cancel()
        
        mediaCodec?.stop()
        mediaCodec?.release()
        
        udpSocket?.close()
        clientSocket?.close()
        serverSocket?.close()
    }
}
