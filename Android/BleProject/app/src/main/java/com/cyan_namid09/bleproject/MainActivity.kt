package com.cyan_namid09.bleproject

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cyan_namid09.bleproject.Bluetooth.AppBluetooth
import com.cyan_namid09.bleproject.databinding.ActivityMainBinding
import java.util.*


private const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBluetooth: AppBluetooth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bluetoothの初期セットアップ(MainActivity)
        AppBluetooth.initialize(applicationContext)
        this.appBluetooth = AppBluetooth.shared

        // アプリがBluetoothを利用可能かつ使用許可を得ていることを確認
        // 使用許可がない場合は、許可ダイアログを表示
        appBluetooth.manager.adapter.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        binding.advertiseButton.setOnClickListener {
            appBluetooth.advertise()
        }

        binding.notifyButton.setOnClickListener {
            val nowTime: ByteArray = System.currentTimeMillis().toString().toByteArray()
            appBluetooth.notify(value = nowTime)
        }
    }
}