package com.oudmon.ble.base.request;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;



import java.util.UUID;

/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 */

public class ReadRequest extends BaseRequest {

    public ReadRequest(UUID serviceUuid, UUID charUuid) {
        super(serviceUuid, charUuid);
    }

    @Override
    public boolean execute(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

}
