package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 启动后的第一条指令会从rsp返回，后续的数据会从notify返回。数据优先返回req，没有req，才是notify
 */

public class StartHeartRateRsp extends BaseRspCmd {

    private byte type;
    private byte errCode;
    private int value;
    private int sbp;
    private int dbp;

    @Override
    public boolean acceptData(byte[] data) {
        type = data[0];
        errCode = data[1];
        value = data[2] & 0xff;
        if (data.length >= 5) {
            sbp = data[3];
            if(sbp <0){
                sbp = data[3] & 0xff;
            }

            dbp=data[4];
            if(dbp <0){
                dbp = data[4] & 0xff;
            }

        }
        return false;
    }

    public byte getType() {
        return type;
    }

    public byte getErrCode() {
        return errCode;
    }

    public int getValue() {
        return value;
    }

    public int getSbp() {
        return Math.abs(sbp);
    }

    public int getDbp() {
        return Math.abs(dbp);
    }

}
