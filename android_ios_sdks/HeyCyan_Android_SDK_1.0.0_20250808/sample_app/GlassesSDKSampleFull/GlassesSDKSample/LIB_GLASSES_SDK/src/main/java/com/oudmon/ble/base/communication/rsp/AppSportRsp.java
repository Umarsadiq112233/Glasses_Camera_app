package com.oudmon.ble.base.communication.rsp;
import com.oudmon.ble.base.communication.utils.ByteUtil;

import java.util.Arrays;

public class AppSportRsp extends BaseRspCmd{
    private int gpsStatus;
    private int timeStamp = 0;
    @Override
    public boolean acceptData(byte[] data) {
        gpsStatus=data[0];
        if(gpsStatus==6){
            timeStamp =(ByteUtil.bytesToInt(Arrays.copyOfRange(data, 2, 6)));
        }
        return false;
    }



    public int getTimeStamp() {
        return timeStamp;
    }

    public int getGpsStatus(){
        return gpsStatus;
    }
}
