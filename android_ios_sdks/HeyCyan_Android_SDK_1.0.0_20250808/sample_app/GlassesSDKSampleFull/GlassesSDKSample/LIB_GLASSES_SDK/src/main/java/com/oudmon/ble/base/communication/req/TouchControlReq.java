package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;

public class TouchControlReq extends MixtureReq {
    boolean touch=false;
    public static TouchControlReq getReadInstance(boolean touch) {

        return new TouchControlReq(touch);
    }

    public TouchControlReq() {
        super(Constants.CMD_DEVICE_TOUCH);
        subData = new byte[] {0x01};
    }



    public TouchControlReq(boolean touch) {
        super(Constants.CMD_DEVICE_TOUCH);
        if(touch){
            subData = new byte[] {0x01,0x00};
        }else {
            subData = new byte[] {0x01,0x01};
        }
    }



    private TouchControlReq(int type,boolean touch,int strength){
        super(Constants.CMD_DEVICE_TOUCH);
        if(touch){
            subData = new byte[] {0x02,0x00, (byte) type};
        }else {
            subData = new byte[] {0x02,0x01, (byte) type, (byte) strength};
        }
    }

    public static TouchControlReq getWriteInstance(int type,boolean touch,int strength){
        return new TouchControlReq(type,touch,strength);
    }
}
