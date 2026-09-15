package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class StopHeartRateRsp extends BaseRspCmd {

    private byte type;
    private byte errCode;
    private byte value;
    private byte sbp;
    private byte dbp;

    @Override
    public boolean acceptData(byte[] data) {
        type = data[0];
        errCode = data[1];
        value = data[2];
        if (data.length >= 5) {
            sbp = data[3];
            dbp = data[4];
        }
        return false;
    }

    /**
     * 0x01: 表示心率
     * 0x02: 表示血压
     * 0x03: 表示血氧
     * 0x04: 表示疲劳度
     * 0x05: 表示一键测量
     *
     * @return
     */
    public byte getType() {
        return type;
    }

    public byte getErrCode() {
        return errCode;
    }

    public byte getValue() {
        return value;
    }

    public byte getSbp() {
        return sbp;
    }

    public byte getDbp() {
        return dbp;
    }
}
