package com.cyan_namid09.bleproject.Bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.*


private const val TAG = "AppBluetooth"

class AppBluetooth(appContext: Context, observable: Subject<String>) {
    val manager: BluetoothManager by lazy { appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val advertiser: BluetoothLeAdvertiser by lazy { manager.adapter.bluetoothLeAdvertiser }
    private val gattServer: BluetoothGattServer by lazy { manager.openGattServer(appContext, gattServerCallback) }
    private val service: BluetoothGattService by lazy {
        BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY).apply {
            this.addCharacteristic(notifyCharacteristic)
        }
    }
    private val notifyCharacteristic: BluetoothGattCharacteristic by lazy {
        BluetoothGattCharacteristic(
            UUID_CHARACTERISTIC,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        ).apply {
            this.value = "SAMPLE".toByteArray()
            this.addDescriptor(cccd)
        }
    }
    private val cccd: BluetoothGattDescriptor by lazy {
        BluetoothGattDescriptor(UUID_CCCD, BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ).apply {
            this.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
    }
    private var connectedDevice: BluetoothDevice? = null

    fun advertise() {
        // Notifyが使えるCharacteristicを含んだServiceをServerに設定
        gattServer.addService(service)

        advertiser.startAdvertising(
            AdvertiseSettings.Builder().build(),
            AdvertiseData.Builder().apply { addServiceUuid(ParcelUuid(UUID_SERVICE)) }.build(),
            object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    Log.d(TAG, "Start Advertising")
                }

                override fun onStartFailure(errorCode: Int) {
                    Log.e(TAG, "onStartFailure. error code is $errorCode")
                    when (errorCode) {
                        ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> Log.e(TAG, "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS")
                        ADVERTISE_FAILED_ALREADY_STARTED -> Log.e(TAG, "ADVERTISE_FAILED_ALREADY_STARTED")
                        ADVERTISE_FAILED_DATA_TOO_LARGE -> Log.e(TAG, "ADVERTISE_FAILED_DATA_TOO_LARGE")
                        ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> Log.e(TAG, "ADVERTISE_FAILED_FEATURE_UNSUPPORTED")
                        ADVERTISE_FAILED_INTERNAL_ERROR -> Log.e(TAG, "ADVERTISE_FAILED_INTERNAL_ERROR")
                    }
                }
            }
        )
    }

    fun notify(value: ByteArray) {
        val device = this.connectedDevice
        if (device != null) {
            notifyCharacteristic.value = value
            val notifyFlag = gattServer.notifyCharacteristicChanged(device, notifyCharacteristic, false)
            if (notifyFlag) Log.d(TAG, "succeeded notification") else Log.e(TAG, "failed notification")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "STATE CONNECTED")
                    connectedDevice = device
                }
                BluetoothProfile.STATE_CONNECTING -> {  // Peripheralでは呼ばれない？
                    Log.d(TAG, "STATE CONNECTING")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "STATE DISCONNECTED")
                    connectedDevice = null
                }
                BluetoothProfile.STATE_DISCONNECTING -> {  // // Peripheralでは呼ばれない？
                    Log.d(TAG, "STATE DISCONNECTING")
                }
            }
        }

        // Characteristic
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            Log.d(TAG, "Characteristic Read Request")
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS , offset, characteristic?.value ?: "NONE".toByteArray())
        }
        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.d(TAG, "Characteristic Write Request")
            characteristic?.value = value
            // subjectにnextで値を流す。
            observable.onNext(String(value ?: "".toByteArray()))
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }
        // Descriptor
        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            Log.d(TAG, "Descriptor Read Request")
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, "NONE".toByteArray())
        }
        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Log.d(TAG, "Descriptor Write Request")
            descriptor?.value = value
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "sent operation succeeded")
                }
                else -> {
                    Log.d(TAG, "sent operation failed")
                }
            }
        }

    }

    companion object {
        lateinit var shared: AppBluetooth

        fun initialize(appContext: Context, observable: Subject<String>) {
            shared = AppBluetooth(appContext, observable)
        }
    }
}
