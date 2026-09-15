package com.oudmon.ble.base.request;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import java.util.UUID;
/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 */

public abstract class BaseRequest {
    public boolean writeRequest;
    private UUID serviceUuid;
    private UUID charUuid;

    public BaseRequest(UUID serviceUuid, UUID charUuid) {
        this.serviceUuid = serviceUuid;
        this.charUuid = charUuid;
    }

    public UUID getServiceUuid() {
        return serviceUuid;
    }

    public UUID getCharUuid() {
        return charUuid;
    }


    public abstract boolean execute(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic);

    /**
     * 需要初始化Characteristic，
     * readRssi的时候不需要初始化
     * @return init
     */
    public boolean needInitCharacteristic() {
        return true;
    }

}
