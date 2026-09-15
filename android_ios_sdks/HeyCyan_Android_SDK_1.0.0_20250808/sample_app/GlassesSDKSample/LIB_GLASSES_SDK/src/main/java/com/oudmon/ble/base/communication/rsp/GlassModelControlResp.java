package com.oudmon.ble.base.communication.rsp;
import android.util.Log;

import com.oudmon.ble.base.communication.utils.ByteUtil;
import java.util.Arrays;

public class GlassModelControlResp extends BaseRspCmd {
    private int dataType;
    private int glassWorkType;
    private int imageCount;
    private int videoCount;
    private String wifiMac;

    @Override
    public boolean acceptData(byte[] data) {
        Log.i(TAG,ByteUtil.byteArrayToString(data));
        //02 01 04 01 60c22a09990a00000000
        dataType=data[1];
        if(dataType ==4){
            imageCount =(ByteUtil.bytesToInt(Arrays.copyOfRange(data, 2, 4)));
            videoCount =(ByteUtil.bytesToInt(Arrays.copyOfRange(data, 4, 6)));
        }else if(dataType==1){
            glassWorkType=data[2];
            wifiMac=String.format("%02X", data[4])+":"+String.format("%02X", data[5])+":"+String.format("%02X", data[6])+":"+String.format("%02X", data[7])+":"+String.format("%02X", data[8])+":"+String.format("%02X", data[9]);
        }
        return false;
    }

    public int getDataType() {
        return dataType;
    }

    public int getGlassWorkType() {
        return glassWorkType;
    }

    public int getImageCount() {
        return imageCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public String getWifiMac() {
        return wifiMac;
    }
}
