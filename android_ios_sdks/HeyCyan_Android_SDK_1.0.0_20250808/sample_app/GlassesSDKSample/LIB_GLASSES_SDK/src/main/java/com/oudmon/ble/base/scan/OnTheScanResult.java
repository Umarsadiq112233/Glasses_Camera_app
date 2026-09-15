package com.oudmon.ble.base.scan;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 */

public interface OnTheScanResult {

    void onResult(BluetoothDevice bluetoothDevice);

    void onScanFailed(int errorCode);
}
