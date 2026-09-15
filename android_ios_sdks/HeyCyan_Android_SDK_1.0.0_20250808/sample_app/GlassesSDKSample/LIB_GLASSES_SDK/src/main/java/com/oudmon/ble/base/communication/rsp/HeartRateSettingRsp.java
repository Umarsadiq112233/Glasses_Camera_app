package com.oudmon.ble.base.communication.rsp;

import com.oudmon.ble.base.communication.utils.ByteUtil;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class HeartRateSettingRsp extends MixtureRsp {


    private boolean isEnable;
    private int heartInterval;
    private int startInterval;


    @Override
    protected void readSubData(byte[] subData) {
        isEnable = subData[1] == 0x01;
        heartInterval= ByteUtil.byteToInt(subData[2]);
        int start=ByteUtil.byteToInt(subData[3]);
        if(start==0){
            startInterval=5;
        }else {
            startInterval=start;
        }
    }

    public boolean isEnable() {
        return isEnable;
    }

    public int getHeartInterval() {
        return heartInterval;
    }

    public int getStartInterval() {
        return startInterval;
    }

    public void setStartInterval(int startInterval) {
        this.startInterval = startInterval;
    }
}
