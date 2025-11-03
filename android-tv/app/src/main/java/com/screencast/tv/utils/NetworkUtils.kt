package com.screencast.tv.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.NetworkInterface
import java.util.*

object NetworkUtils {
    
    fun getLocalIPAddress(context: Context): String? {
        android.util.Log.d("Network", "开始获取IP地址")
        
        try {
            // 检查网络权限
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: throw Exception("无法访问WiFi服务")
                
            // 尝试获取WiFi IP
            val wifiInfo = wifiManager.connectionInfo
            val ipInt = wifiInfo.ipAddress
            
            if (ipInt != 0) {
                val ipAddress = String.format(
                    Locale.getDefault(),
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
                android.util.Log.d("Network", "获取到WiFi IP: $ipAddress")
                return ipAddress
            }
            
            // 如果WiFi方式获取失败，尝试遍历网络接口
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress?.indexOf(':') == -1) {
                        android.util.Log.d("Network", "获取到网络接口IP: ${address.hostAddress}")
                        return address.hostAddress
                    }
                }
            }
            
            android.util.Log.w("Network", "未找到可用IP地址")
            return null
            
        } catch (e: Exception) {
            android.util.Log.e("Network", "获取IP地址失败", e)
            return null
        }
        try {
            // 尝试获取WiFi IP
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipInt = wifiInfo.ipAddress
            
            if (ipInt != 0) {
                return String.format(
                    Locale.getDefault(),
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            }
            
            // 如果WiFi方式获取失败，尝试遍历网络接口
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress?.indexOf(':') == -1) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return null
    }
}
