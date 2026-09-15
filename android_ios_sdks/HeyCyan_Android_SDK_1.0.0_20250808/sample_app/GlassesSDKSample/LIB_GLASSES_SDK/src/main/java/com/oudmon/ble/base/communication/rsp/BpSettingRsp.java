package com.oudmon.ble.base.communication.rsp;

import com.oudmon.ble.base.communication.entity.StartEndTimeEntity;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class BpSettingRsp extends MixtureRsp {

    private boolean isEnable;
    private StartEndTimeEntity startEndTimeEntity;
    private int multiple;

    @Override
    protected void readSubData(byte[] subData) {
        isEnable = subData[1] == 0x01;
        startEndTimeEntity = new StartEndTimeEntity(subData[2], subData[3], subData[4], subData[5]);
        multiple = subData[6];
    }

    public boolean isEnable() {
        return isEnable;
    }

    public StartEndTimeEntity getStartEndTimeEntity() {
        return startEndTimeEntity;
    }

    public int getMultiple() {
        return multiple;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
