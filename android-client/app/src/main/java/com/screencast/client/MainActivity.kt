package com.screencast.client

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private val CAMERA_PERMISSION_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanButton = findViewById<Button>(R.id.scanButton)
        val bitrateSpinner = findViewById<android.widget.Spinner>(R.id.bitrateSpinner)
        val framerateSpinner = findViewById<android.widget.Spinner>(R.id.framerateSpinner)

        // 初始化码率选项
        val bitrateAdapter = android.widget.ArrayAdapter.createFromResource(
            this,
            R.array.bitrate_options,
            android.R.layout.simple_spinner_item
        )
        bitrateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bitrateSpinner.adapter = bitrateAdapter

        // 初始化帧率选项
        val framerateAdapter = android.widget.ArrayAdapter.createFromResource(
            this,
            R.array.framerate_options,
            android.R.layout.simple_spinner_item
        )
        framerateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        framerateSpinner.adapter = framerateAdapter

        // 读取保存的设置
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        bitrateSpinner.setSelection(prefs.getInt("bitrate_index", 0))
        framerateSpinner.setSelection(prefs.getInt("framerate_index", 2)) // 默认30fps

        // 保存设置
        bitrateSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putInt("bitrate_index", position).apply()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })
        framerateSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putInt("framerate_index", position).apply()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        scanButton.setOnClickListener {
            checkCameraPermission()
        }

        val stopCastButton = findViewById<Button>(R.id.stopCastButton)
        stopCastButton.setOnClickListener {
            val intent = Intent(this, com.screencast.client.service.ScreenCastService::class.java)
            stopService(intent)
            android.widget.Toast.makeText(this, "已终止投屏", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            startScanActivity()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanActivity()
            } else {
                Toast.makeText(this, "需要相机权限以扫描二维码", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startScanActivity() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)
    }
}
