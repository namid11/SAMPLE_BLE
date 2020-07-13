package com.cyan_namid09.bleproject

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.database.Observable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.cyan_namid09.bleproject.Bluetooth.AppBluetooth
import com.cyan_namid09.bleproject.databinding.ActivityMainBinding
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.*
import kotlin.concurrent.thread


private const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBluetooth: AppBluetooth

    private val characteristicObservable: Subject<String> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observableでデータ書き込み時の値を表示
        // handlerで強引に表示処理してるけど許して...
        val handler = Handler()
        this.characteristicObservable.subscribe { value ->
            thread {
                handler.post {
                    binding.characteristicText.text = value
                }
            }
        }


        // Bluetoothの初期セットアップ(MainActivity)
        AppBluetooth.initialize(applicationContext, characteristicObservable)
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