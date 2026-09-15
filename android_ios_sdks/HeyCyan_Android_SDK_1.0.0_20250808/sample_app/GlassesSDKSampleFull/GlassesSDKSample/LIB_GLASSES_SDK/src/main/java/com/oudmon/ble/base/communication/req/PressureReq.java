package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class PressureReq extends MixtureReq {
    private byte index;

    public PressureReq(byte index) {
        super(Constants.CMD_PRESSURE);
        subData=new byte[]{index};
    }

}
