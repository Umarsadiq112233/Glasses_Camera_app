package com.oudmon.ble.base.bluetooth;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * @author gs ,
 * @date swatch_device_text12/23
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class BleAction {
    /** Intent for broadcast */
    public static final String BLE_NOT_SUPPORTED = "com.swatchdevice.pro.sdk.ble.not_supported";
    public static final String BLE_NO_BT_ADAPTER = "com.swatchdevice.pro.sdk.ble.no_bt_adapter";
    public static final String BLE_STATUS_ABNORMAL = "com.swatchdevice.pro.sdk.ble.status_abnormal";
    public static final String BLE_START_CONNECT = "com.swatchdevice.pro.sdk.ble.start_connect";
    public static final String BLE_GATT_CONNECTED = "com.swatchdevice.pro.sdk.ble.gatt_connected";
    public static final String BLE_GATT_DISCONNECTED = "com.swatchdevice.pro.sdk.ble.gatt_disconnected";
    public static final String BLE_SERVICE_DISCOVERED = "com.swatchdevice.pro.sdk.ble.service_discovered";
    public static final String BLE_CHARACTERISTIC_READ = "com.swatchdevice.pro.sdk.ble.characteristic_read";
    public static final String BLE_CHARACTERISTIC_DISCOVERED = "com.swatchdevice.pro.sdk.ble.characteristic_notification_qc";
    public static final String BLE_CHARACTERISTIC_WRITE = "com.swatchdevice.pro.characteristic_write_qc";
    public static final String BLE_CHARACTERISTIC_CHANGED = "com.swatchdevice.pro.characteristic_changed_qc";
    public static final String BLE_NO_CALLBACK = "com.swatchdevice.pro.sdk.ble.BLE_NO_CALLBACK";
    public static final String BLE_STATUS = "com.swatchdevice.pro.sdk.ble.BLE_STATUS";

    /** Intent extras */
    public static final String EXTRA_DEVICE = "DEVICE";
    public static final String EXTRA_ADDR = "ADDRESS";
    public static final String EXTRA_CONNECTED = "CONNECTED";
    public static final String EXTRA_STATUS = "STATUS";
    public static final String EXTRA_CHARACTER_UUID = "CHARACTER_UUID";
    public static final String EXTRA_VALUE = "VALUE";
    public static final String EXTRA_DATA = "DATA";
    public static final String EXTRA_BLE_STATUS = "EXTRA_STATUS";
    public static final String EXTRA_BLE_NEW_STATE = "EXTRA_BLE_NEW_STATE";



    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLE_NOT_SUPPORTED);
        intentFilter.addAction(BLE_NO_BT_ADAPTER);
        intentFilter.addAction(BLE_STATUS_ABNORMAL);
        intentFilter.addAction(BLE_GATT_CONNECTED);
        intentFilter.addAction(BLE_GATT_DISCONNECTED);
        intentFilter.addAction(BLE_SERVICE_DISCOVERED);
        intentFilter.addAction(BLE_CHARACTERISTIC_READ);
        intentFilter.addAction(BLE_CHARACTERISTIC_DISCOVERED);
        intentFilter.addAction(BLE_CHARACTERISTIC_WRITE);
        intentFilter.addAction(BLE_CHARACTERISTIC_CHANGED);
        intentFilter.addAction(BLE_START_CONNECT);
        intentFilter.addAction(BLE_NO_CALLBACK);
        intentFilter.addAction(BLE_STATUS);
        return intentFilter;
    }

    public static IntentFilter getDeviceIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        //系统修改时间监听
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);

        //新增其它系统广播
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_CAMERA_BUTTON);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        return intentFilter;
    }

}
