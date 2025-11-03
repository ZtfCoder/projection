package com.screencast.client.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer

class ScreenCastService : Service() {
    
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaCodec: MediaCodec? = null
    private var tcpSocket: Socket? = null
    private var udpSocket: DatagramSocket? = null
    private var serverAddress: InetAddress? = null
    private var serverPort: Int = 0
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var screenWidth = 1280
    private var screenHeight = 720
    private var bitRate: Int? = 2000000  // 可为 null 表示无限制
    private var frameRate: Int = 30
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ScreenCastChannel"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: return START_NOT_STICKY
        val data = intent.getParcelableExtra<Intent>("data") ?: return START_NOT_STICKY
        val serverIp = intent.getStringExtra("server_ip") ?: return START_NOT_STICKY
        serverPort = intent.getIntExtra("server_port", 0)

        // 读取码率和帧率参数
        val bitrateValue = intent.getIntExtra("bitrate", -1)
        bitRate = if (bitrateValue > 0) bitrateValue else null
        frameRate = intent.getIntExtra("framerate", 30)

        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            try {
                serverAddress = InetAddress.getByName(serverIp)
                connectToServer()
                startScreenCapture(resultCode, data)
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
            }
        }

        return START_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "投屏服务",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("投屏中")
            .setContentText("正在将屏幕投射到投影仪")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
    
    private fun connectToServer() {
        // 建立TCP连接用于控制
        tcpSocket = Socket(serverAddress, serverPort)
        val output = DataOutputStream(tcpSocket!!.getOutputStream())
        output.writeUTF("READY")
        output.flush()
        
        // 创建UDP socket用于视频传输
        udpSocket = DatagramSocket()
    }
    
    private fun startScreenCapture(resultCode: Int, data: Intent) {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        
        // 获取屏幕尺寸
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        
        // 配置视频编码器
        setupMediaCodec()
        
        // 创建VirtualDisplay
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCast",
            screenWidth,
            screenHeight,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaCodec?.createInputSurface(),
            null,
            null
        )
        
        // 开始编码
        startEncoding()
    }
    
    private fun setupMediaCodec() {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, screenWidth, screenHeight)
        if (bitRate != null) {
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate!!)
        }
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec?.start()
    }
    
    private fun startEncoding() {
        serviceScope.launch {
            val bufferInfo = MediaCodec.BufferInfo()
            
            while (isActive) {
                try {
                    val outputBufferId = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000) ?: continue
                    
                    if (outputBufferId >= 0) {
                        val outputBuffer = mediaCodec?.getOutputBuffer(outputBufferId)
                        
                        if (outputBuffer != null && bufferInfo.size > 0) {
                            val data = ByteArray(bufferInfo.size)
                            outputBuffer.get(data)
                            
                            // 通过UDP发送视频数据
                            sendVideoData(data)
                        }
                        
                        mediaCodec?.releaseOutputBuffer(outputBufferId, false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }
    
    private fun sendVideoData(data: ByteArray) {
        try {
            val packet = DatagramPacket(data, data.size, serverAddress, serverPort + 1)
            udpSocket?.send(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        serviceScope.cancel()
        
        virtualDisplay?.release()
        mediaCodec?.stop()
        mediaCodec?.release()
        mediaProjection?.stop()
        
        tcpSocket?.close()
        udpSocket?.close()
    }
}
