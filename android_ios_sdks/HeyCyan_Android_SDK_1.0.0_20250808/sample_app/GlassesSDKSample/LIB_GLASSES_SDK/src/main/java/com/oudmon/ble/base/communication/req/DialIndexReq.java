package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

public class DialIndexReq extends MixtureReq {

    public DialIndexReq() {
        super(Constants.CMD_DEVICE_DIAL_INDEX);
        subData = new byte[] {0x00};
    }


    private DialIndexReq(int index) {
        super(Constants.CMD_DEVICE_DIAL_INDEX);
        subData = new byte[] {0x01, (byte)index};
    }


    public static DialIndexReq getReadInstance() {
        return new DialIndexReq();
    }

    public static DialIndexReq getWriteInstance(int index) {
        return new DialIndexReq(index);
    }
}
