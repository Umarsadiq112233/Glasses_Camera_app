package com.oudmon.ble.base.scan;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;

/****
 * @author gs 20200813
 */
public abstract class BleScannerCompat {

    private static BleScannerCompat mInstance;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ScanWrapperCallback scanWrapperCallback;
    public boolean scanning;


    public static BleScannerCompat getScanner(Context context) {
        if (mInstance != null){
            return mInstance;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return mInstance = new BluetoothScannerImplLollipop(context);
        }else {
            return mInstance = new BluetoothScannerImplJB();
        }
    }

    public void startScan(ScanWrapperCallback scanWrapperCallback){
        scanning=true;
        this.scanWrapperCallback = scanWrapperCallback;
        scanWrapperCallback.onStart();
    }

    //TODO 添加过滤扫描设备的接口(uuid,设备名,设备地址等)

    public void stopScan(){
        scanning=false;
        if (scanWrapperCallback != null){
            scanWrapperCallback.onStop();
        }
    }

    public boolean isScanning() {
        return scanning;
    }
}
