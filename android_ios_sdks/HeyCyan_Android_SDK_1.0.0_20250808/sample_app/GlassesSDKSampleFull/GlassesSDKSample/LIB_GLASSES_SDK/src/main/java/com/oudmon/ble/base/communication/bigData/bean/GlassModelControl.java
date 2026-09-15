package com.oudmon.ble.base.communication.bigData.bean;

public class GlassModelControl{

    protected byte[] subData;
    public GlassModelControl(byte [] sendData) {
        this.subData = sendData;
    }


    public byte[] getSubData() {
        return subData;
    }
}
