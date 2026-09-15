package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.9防丢报警命令
 */

public class IntellReq extends MixtureReq {

    private boolean isEnable;
    private byte delaySecond;

    private IntellReq() {
        super(Constants.CMD_INTELL);
        subData=new byte[]{0x01};
    }

    private IntellReq(boolean isEnable, byte delaySecond) {
        super(Constants.CMD_INTELL);
        subData = new byte[]{0x02, (byte) (isEnable ? 0x01 : 0x02),delaySecond};
    }

    public static IntellReq getReadInstance(){
        return new IntellReq();
    }

    public static IntellReq getWriteInstance(boolean isEnable, byte delaySecond) {
        return new IntellReq(isEnable, delaySecond);
    }
}
