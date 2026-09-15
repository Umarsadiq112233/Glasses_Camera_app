package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class HRVReq extends MixtureReq {
    private byte index;

    public HRVReq(byte index) {
        super(Constants.CMD_HRV);
        subData=new byte[]{index};
    }

}
