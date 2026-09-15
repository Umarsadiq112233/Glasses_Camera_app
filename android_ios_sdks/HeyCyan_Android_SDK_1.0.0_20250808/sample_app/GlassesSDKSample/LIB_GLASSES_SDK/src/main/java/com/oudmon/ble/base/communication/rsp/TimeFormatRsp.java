package com.oudmon.ble.base.communication.rsp;

import com.oudmon.ble.base.communication.utils.ByteUtil;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class TimeFormatRsp extends MixtureRsp {

    private boolean is24;
    private boolean isMetric;
    public int gender;
    public int age;
    public int height;
    public int weight;
    public int sbp;
    public int dbp;
    public int warmingHeart;
    public int open =2;



    @Override
    protected void readSubData(byte[] subData) {
        is24 = subData[1] == 0x00;
        isMetric = subData[2] == 0x00;
        gender = subData[3];
        age = subData[4];
        height = subData[5];
        weight = subData[6];
        sbp = subData[7];
        dbp = subData[8];
        warmingHeart = ByteUtil.byteToInt(subData[9]);
        open = subData[10];
    }

    public boolean is24() {
        return is24;
    }

    public boolean isMetric(){
        return isMetric;
    }

    @Override
    public String toString() {
        return "TimeFormatRsp{" +
                "is24=" + is24 +
                '}';
    }
}
