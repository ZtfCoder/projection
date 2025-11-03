package com.screencast.tv.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.NetworkInterface
import java.util.*

object NetworkUtils {
    
    fun getLocalIPAddress(context: Context): String? {
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
