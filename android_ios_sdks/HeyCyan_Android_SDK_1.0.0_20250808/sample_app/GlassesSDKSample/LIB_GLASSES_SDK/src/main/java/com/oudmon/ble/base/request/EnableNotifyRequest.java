package com.oudmon.ble.base.request;

import static com.oudmon.ble.base.bluetooth.QCDataParser.TAG;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;



import java.util.UUID;

/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 */

public class EnableNotifyRequest extends BaseRequest {
    private UUID GATT_NOTIFY_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private boolean isEnable = true;
    private ListenerCallback callback;

    public EnableNotifyRequest(UUID serviceUuid, UUID charUuid,ListenerCallback callback) {
        super(serviceUuid, charUuid);
        this.callback=callback;
    }

    public EnableNotifyRequest(UUID serviceUuid, UUID charUuid, boolean isEnable) {
        super(serviceUuid, charUuid);
        this.isEnable = isEnable;
    }

    @Override
    public boolean execute(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if(bluetoothGatt==null){
            if(callback!=null){
                callback.enable(false);
            }
            return false;
        }
        boolean b = bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, isEnable);
        if (!b) {
             Log.i(TAG, "open local notify failed");
            if(callback!=null){
                callback.enable(false);
            }
            return false;
        }
        // enable notifications on the device
        final BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(GATT_NOTIFY_CONFIG);
        if (descriptor == null) {
             Log.i(TAG,"descriptor is null, execute failed");
            if(callback!=null){
                callback.enable(false);
            }
            return false;
        }
        descriptor.setValue(isEnable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        boolean flag=bluetoothGatt.writeDescriptor(descriptor);
//         Log.i(TAG,"gatt.writeDescriptor(" + descriptor.getUuid() + ", value=0x01-00)-----"+flag);
        if(callback!=null){
            callback.enable(flag);
        }
        return flag;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public interface ListenerCallback{
        void enable(boolean result);
    }

}
