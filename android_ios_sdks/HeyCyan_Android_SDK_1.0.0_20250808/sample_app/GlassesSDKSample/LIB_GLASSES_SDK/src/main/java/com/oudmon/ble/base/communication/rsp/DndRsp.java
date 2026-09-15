package com.oudmon.ble.base.communication.rsp;

import com.oudmon.ble.base.communication.entity.StartEndTimeEntity;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class DndRsp extends MixtureRsp {
    private boolean isEnable;
    private StartEndTimeEntity dndEntity;
    private boolean manualDND;


    @Override
    protected void readSubData(byte[] subData) {
        isEnable = subData[1] == 0x01;
        manualDND= subData[6] ==0x01;
        dndEntity = new StartEndTimeEntity(subData[2], subData[3], subData[4], subData[5]);
    }

    public boolean isEnable() {
        return isEnable;
    }

    public boolean isManualDND() {
        return manualDND;
    }


    public StartEndTimeEntity getDndEntity() {
        return dndEntity;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
