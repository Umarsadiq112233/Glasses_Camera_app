package com.oudmon.ble.base.scan;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
/**
 * @author gs
 */
class BluetoothScannerImplJB extends BleScannerCompat {

    @Override
    public void startScan(ScanWrapperCallback scanWrapperCallback) {
        super.startScan(scanWrapperCallback);
        scanning=true;
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    @Override
    public void stopScan() {
        super.stopScan();
        scanning=false;
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
            scanWrapperCallback.onLeScan(device, rssi, scanRecord);
        }
    };
}
