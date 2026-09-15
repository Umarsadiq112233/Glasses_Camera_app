package com.oudmon.ble.base.scan;
import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import com.oudmon.ble.base.util.AppUtil;
import com.oudmon.qc_utils.bluetooth.BluetoothUtils;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BluetoothScannerImplLollipop extends BleScannerCompat {
    private static final String TAG = "BluetoothScannerImplLol";
    private BluetoothLeScanner scanner;
    private ScanSettings scanSettings;
    private List<ScanFilter> filters = new ArrayList<>();
    private Context context;


    public BluetoothScannerImplLollipop(Context context) {
        this.context = context;
    }

    @Override
    public void startScan(ScanWrapperCallback scanWrapperCallback) {
        super.startScan(scanWrapperCallback);
        scanning = true;
        if (scanner == null) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        if (!BluetoothUtils.isEnabledBluetooth(context)) {
            return;
        }
        setScanSettings();
        if (scanner != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            scanner.startScan(filters, scanSettings, scannerCallback);
        }

    }

    @Override
    public void stopScan() {
        super.stopScan();
        scanning=false;
        if(!BluetoothUtils.isEnabledBluetooth(context)){
            return;
        }
        if (scanner == null) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        scanner.stopScan(scannerCallback);
    }

    private ScanCallback scannerCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            byte[] scanRecord = result.getScanRecord().getBytes();
            if (scanWrapperCallback != null){
                scanWrapperCallback.onLeScan(device, result.getRssi(), scanRecord);
            }
            ScanRecord parseRecord = ScanRecord.parseFromBytes(scanRecord);
            if (parseRecord != null && scanWrapperCallback != null) {
                scanWrapperCallback.onParsedData(device, parseRecord);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            if(results!=null&& !results.isEmpty() && scanWrapperCallback!=null){
                scanWrapperCallback.onBatchScanResults(results);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
           Log.i(TAG, "Scan Failed Error Code: " + errorCode);
            if (scanWrapperCallback != null){
                scanWrapperCallback.onScanFailed(errorCode);
            }
        }
    };


    private void setScanSettings() {
        /**兼容手机，采用两种机制判断***/
        boolean background = AppUtil.isBackground(context);
        boolean background1 = AppUtil.isApplicationBroughtToBackground(context);
        if (background || background1){
            /**8.0以上手机后台扫描，必须开启,后台用低功耗扫描**/
//            filters.add(new ScanFilter.Builder()
//                    .setServiceUuid(ParcelUuid.fromString("0000fee7-0000-1000-8000-00805f9b34fb"))
//                    .build());
            ScanSettings.Builder builder = new ScanSettings.Builder();
            builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
            builder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            scanSettings = builder.build();
        }else {
            filters.clear();
            ScanSettings.Builder builder = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            builder.setReportDelay(0);
            scanSettings = builder.build();
        }
    }
}
