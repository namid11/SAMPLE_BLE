package com.cyan_namid09.bleproject.Bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.*

object BLEParams {
    val UUID_SERVICE: UUID = UUID.fromString("0783266c-3115-4a24-8ace-b61baed1ea3a")
    val UUID_CHARACTERISTIC: UUID = UUID.fromString("5ac98f44-c344-11ea-87d0-0242ac130003")
    val UUID_CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    val service = BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY).apply {
        this.addCharacteristic(notifyCharacteristic)
    }
    val notifyCharacteristic: BluetoothGattCharacteristic = BluetoothGattCharacteristic(
        UUID_CHARACTERISTIC,
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
    ).apply {
        this.addDescriptor(cccd)
    }
    val cccd: BluetoothGattDescriptor = BluetoothGattDescriptor(UUID_CCCD, BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ).apply {
            this.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
    }
}