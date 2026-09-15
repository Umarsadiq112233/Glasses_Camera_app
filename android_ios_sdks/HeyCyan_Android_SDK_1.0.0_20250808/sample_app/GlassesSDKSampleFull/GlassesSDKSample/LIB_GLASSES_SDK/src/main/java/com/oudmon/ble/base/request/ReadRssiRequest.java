package com.oudmon.ble.base.request;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 */

public class ReadRssiRequest extends BaseRequest {

    private static ReadRssiRequest readRequest = new ReadRssiRequest();

    public static ReadRssiRequest getInstance() {
        return readRequest;
    }

    private ReadRssiRequest() {
        super(null, null);
    }

    @Override
    public boolean execute(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return bluetoothGatt.readRemoteRssi();
    }

    @Override
    public boolean needInitCharacteristic() {
        return false;
    }

}
