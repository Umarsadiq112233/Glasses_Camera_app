package com.oudmon.ble.base.request;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import java.util.UUID;

/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 * TODO:这里还有一个问题,就是 同一个WriteOpAction 执行两次不同的value后，第二次的值会被发送两次，而第一次的值，会被覆盖，这个是在底层处理勒，还是留给上层处理。先留给上层处理吧，因为还要校验ACK回应
 */

public class WriteRequest extends BaseRequest {

    private byte[] value;
    private boolean noRsp = false;

    public WriteRequest(UUID serviceUuid, UUID charUuid) {
        super(serviceUuid, charUuid);
        writeRequest=true;
    }

    private WriteRequest(UUID serviceUuid, UUID charUuid, boolean noResponse) {
        super(serviceUuid, charUuid);
        this.noRsp = noResponse;
        writeRequest=true;
    }

    public static WriteRequest getNoRspInstance(UUID serviceUuid, UUID charUuid) {
        return new WriteRequest(serviceUuid, charUuid, true);
    }


    public void setValue(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public boolean execute(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (value == null) {
            return false;
        }
        try {
            bluetoothGattCharacteristic.setWriteType(noRsp ? BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE : BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            boolean setValue = bluetoothGattCharacteristic.setValue(value);
            boolean result=setValue && bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
