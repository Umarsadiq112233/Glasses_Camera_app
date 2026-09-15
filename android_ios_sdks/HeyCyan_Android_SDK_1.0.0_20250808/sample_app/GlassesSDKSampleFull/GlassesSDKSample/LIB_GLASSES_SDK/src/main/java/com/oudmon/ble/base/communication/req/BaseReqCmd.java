package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 注意，BaseReqCmd 针对只读的数据元，
 * 而如果有读有写的最好是继承 MixtureReq
 */

public abstract class BaseReqCmd {

    protected static final String TAG = "Jxr35";

    protected byte key;
    protected int type;

    public BaseReqCmd(byte key) {
        this.key = key;
    }

    public byte[] getData(){
        byte[] bytes = new byte[Constants.CMD_DATA_LENGTH];
        bytes[0] = key;
        byte[] subData = getSubData();
        if (subData != null) {
            System.arraycopy(subData,0,bytes,1,subData.length);
        }
        addCRC(bytes);
        return bytes;
    }

    protected abstract byte[] getSubData();

    /**
     * 添加CRC校验
     */
    private void addCRC(byte[] data) {
        int crc = 0;
        for (int i = 0; i < data.length - 1; i++) {
            crc += data[i];
        }
        data[data.length - 1] = (byte) (crc & 0xFF);
    }
}
