package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class DisplayStyleRsp extends MixtureRsp {

    private int styleIndex;

    @Override
    protected void readSubData(byte[] subData) {
        styleIndex = subData[1];
    }

    public int getStyleIndex() {
        return styleIndex;
    }


    public void setStyleIndex(int index) {
        styleIndex = index;
    }
}
