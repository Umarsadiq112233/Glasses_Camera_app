package com.oudmon.ble.base.communication.bigData.resp;

import com.oudmon.ble.base.communication.utils.ByteUtil;

import java.util.Arrays;

public class GlassesTouchSupportRsp extends BaseResponse {
    int glassesModel;
    boolean translationSupport;
    boolean wearCheckSupport;
    boolean volumeControl;

    @Override
    public boolean acceptData(byte[] data) {
        try {
            if(data.length<=9){
                glassesModel = ByteUtil.bytesToInt(Arrays.copyOfRange(data, 7, 8));
                translationSupport = ByteUtil.bytesToInt(Arrays.copyOfRange(data, 8, 9))==1;
                wearCheckSupport=true;
            }else if(data.length == 10){
                glassesModel = ByteUtil.bytesToInt(Arrays.copyOfRange(data, 7, 8));
                translationSupport = ByteUtil.bytesToInt(Arrays.copyOfRange(data, 8, 9))==1;
                wearCheckSupport = ByteUtil.bytesToInt(Arrays.copyOfRange(data, 9, 10))==0;
            }else {
                glassesModel = ByteUtil.bytesToInt(Arrays.copyOfRange(data, 7, 8));
                translationSupport = ByteUtil.bytesToInt(Arrays.copyOfRange(data, 8, 9))==1;
                wearCheckSupport = ByteUtil.bytesToInt(Arrays.copyOfRange(data, 9, 10))==0;
                volumeControl = (data[10] & 0x01) != 0;
            }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return false;
    }


    public int getGlassesModel() {
        return glassesModel;
    }

    public boolean isTranslationSupport() {
        return translationSupport;
    }

    public boolean isWearCheckSupport() {
        return wearCheckSupport;
    }

    public boolean isVolumeControl() {
        return volumeControl;
    }
}
