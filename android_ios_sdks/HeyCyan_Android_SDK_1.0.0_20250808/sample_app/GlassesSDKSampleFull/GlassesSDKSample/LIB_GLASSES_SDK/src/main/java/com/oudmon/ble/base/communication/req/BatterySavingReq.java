package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

public class BatterySavingReq extends MixtureReq {
    public BatterySavingReq() {
        super(Constants.CMD_DEVICE_BATTERY_SAVING);
        subData = new byte[] {0x00};
    }

    private BatterySavingReq(boolean open) {
        super(Constants.CMD_DEVICE_BATTERY_SAVING);
        subData = new byte[] {0x01, (byte) (open ? 0x01 : 0x00)};
    }


    public static BatterySavingReq getReadInstance() {
        return new BatterySavingReq();
    }

    public static BatterySavingReq getWriteInstance(boolean open) {
        return new BatterySavingReq(open);
    }
}
