package com.oudmon.ble.base.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import com.oudmon.ble.base.request.BaseRequest;

import java.util.UUID;

/**
 * @author gs ,
 * @date swatch_device_text8/7,
 * <p>
 * "佛主保佑,
 * 永无bug"
 * ble 连接对外提供的监听
 **/
public interface IBleListener {
    /**
     *开始连接
     */
    void startConnect();

    /***
     * gatt 成功建立连接
     * @param device
     */
    void bleGattConnected(BluetoothDevice device);

    /***
     * gatt 断开连接
     * @param device
     */
    void bleGattDisconnect(BluetoothDevice device);

    /**
     * 发现服务
     * @param state
     * @param address
     */
    void bleServiceDiscovered(int state, String address);

    /**
     * 蓝牙报错回调
     * @param status
     * @param newState
     */
    void bleStatus(int status,int newState);

    /**
     * 读特征成功
     * @param address
     * @param uuid
     * @param status
     * @param value
     */
    void bleCharacteristicRead(String address, String uuid, int status, byte[] value);

    /**
     * notify特征
     */
    void bleCharacteristicNotification();

    /**
     * 写特征成功
     * @param address
     * @param uuid
     * @param status
     * @param data
     */
    void bleCharacteristicWrite(String address, String uuid, int status, byte[] data);

    /***
     * 监听特征变化
     * @param address
     * @param uuid
     * @param value
     */
    void bleCharacteristicChanged( String address, String uuid, byte[] value);

    /***
     * 系统没有回调
     */
    void  bleNoCallback();

    boolean execute(final BaseRequest baseCharAction);

    void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);

    void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

    void onDescriptorWrite(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

    boolean isConnected();

}
