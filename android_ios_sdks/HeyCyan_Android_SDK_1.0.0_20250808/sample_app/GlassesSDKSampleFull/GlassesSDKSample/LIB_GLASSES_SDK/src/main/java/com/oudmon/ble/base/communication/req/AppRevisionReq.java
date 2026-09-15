package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;

public class AppRevisionReq extends MixtureReq {


    private AppRevisionReq(int type){
        super(Constants.CMD_DEVICE_REVISION);
        subData = new byte[] {(byte) type};

    }

    public static AppRevisionReq getWriteInstance(int type){
        return new AppRevisionReq(type);
    }
}
