package com.oudmon.ble.base.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author gs ,
 * @date swatch_device_text12/23
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class QCBluetoothCallbackCloneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String uuid;
        byte[] data;
        BluetoothDevice device;
        switch (action) {
            case BleAction.BLE_GATT_CONNECTED:
                device = intent.getParcelableExtra(BleAction.EXTRA_DEVICE);
                connectStatue(device, true);
                break;
            case BleAction.BLE_GATT_DISCONNECTED:
            case BleAction.BLE_NO_CALLBACK:
                device = intent.getParcelableExtra(BleAction.EXTRA_DEVICE);
                connectStatue(device, false);
                break;
            case BleAction.BLE_CHARACTERISTIC_WRITE:
                data = intent.getByteArrayExtra(BleAction.EXTRA_DATA);
                onCommandSend(data);
                break;
            case BleAction.BLE_CHARACTERISTIC_READ:
                uuid = intent.getStringExtra(BleAction.EXTRA_CHARACTER_UUID);
                data = intent.getByteArrayExtra(BleAction.EXTRA_VALUE);
                onCharacteristicRead(uuid, data);
                break;

            case BleAction.BLE_CHARACTERISTIC_CHANGED:
                String address = intent.getStringExtra(BleAction.EXTRA_ADDR);
                uuid = intent.getStringExtra(BleAction.EXTRA_CHARACTER_UUID);
                data = intent.getByteArrayExtra(BleAction.EXTRA_VALUE);
                onCharacteristicChange(address, uuid, data);
                break;
            case BleAction.BLE_SERVICE_DISCOVERED:
                onServiceDiscovered();
                break;
            case BleAction.BLE_STATUS:
                int status = intent.getIntExtra(BleAction.EXTRA_BLE_STATUS, 99);
                int newState = intent.getIntExtra(BleAction.EXTRA_BLE_NEW_STATE, 99);
                bleStatus(status, newState);
                break;

            default:
                break;
        }
    }

    public void connectStatue(BluetoothDevice device, boolean connected) {
    }

    public void onCharacteristicRead(String uuid, byte[] data) {

    }

    public void bleStatus(int status, int newState) {

    }


    public void onServiceDiscovered() {

    }

    public void onCommandSend(byte[] data) {

    }

    public void onCharacteristicChangeFilter(String address, String uuid, byte[] data) {
    }


    public void onCharacteristicChange(String address, String uuid, byte[] data) {
    }

}
