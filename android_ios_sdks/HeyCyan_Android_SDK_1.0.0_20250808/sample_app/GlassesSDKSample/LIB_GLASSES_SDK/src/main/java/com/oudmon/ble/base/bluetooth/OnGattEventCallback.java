package com.oudmon.ble.base.bluetooth;
/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 */

public interface OnGattEventCallback {

    /**
     * read 和 notify都从这个接口返回数据
     * @param data
     */
    void onReceivedData(String uuid, byte[] data);


}
