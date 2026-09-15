package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;


public class MuslimReq extends MixtureReq {
    private byte index;

    public MuslimReq(byte index) {
        super(Constants.CMD_MUSLIM_DATA);
        subData=new byte[]{0x01,index};
    }


    public MuslimReq(boolean clear) {
        super(Constants.CMD_MUSLIM_DATA);
        subData=new byte[]{0x02,0x01};
    }



    public static MuslimReq getWriteInstance(boolean clear) {
        return new MuslimReq(clear);
    }

}
