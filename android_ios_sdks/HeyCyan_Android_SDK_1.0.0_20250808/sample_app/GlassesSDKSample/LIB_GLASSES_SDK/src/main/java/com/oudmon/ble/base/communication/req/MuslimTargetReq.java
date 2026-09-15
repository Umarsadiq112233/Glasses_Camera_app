package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.ByteUtil;


public class MuslimTargetReq extends MixtureReq {
    private byte index;

    public MuslimTargetReq() {
        super(Constants.CMD_MUSLIM_GOAL_DATA);
        subData=new byte[]{0x01,0X01};
    }


    public MuslimTargetReq(int goal) {
        super(Constants.CMD_MUSLIM_GOAL_DATA);
        byte[] a=new byte[]{0x02,0x01};
        subData=ByteUtil.concat(a,ByteUtil.intToByte(goal,4));
    }


}
