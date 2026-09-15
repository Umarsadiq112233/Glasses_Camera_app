package com.oudmon.ble.base.communication.bigData.resp;
import android.util.Log;

import com.oudmon.ble.base.communication.utils.ByteUtil;

import java.util.Arrays;

public class GlassModelControlResponse extends BaseResponse {
    private int dataType;
    private int glassWorkType;
    private int imageCount;
    private int videoCount;
    private int recordCount;
    private String p2pIp;
    private int errorCode=1;
    private int workTypeIng=0;
    private int videoAngle;
    private int videoDuration;
    private int otaStatus;
    private int recordAudioDuration;

    @Override
    public boolean acceptData(byte[] data) {
//        Log.i(TAG,ByteUtil.byteArrayToString(data));
        //bc410a009c2c 02 01 04  01 a0 86 01 00 00 01
        //bc410b000289 0204 080004000100010000
        //bc410a007bab 02 01 04 01 60 c2 2a 0b 55 56
        //bc4105008c3d0201010002

        //bc 41 05 00 fd fa 02 01 12 00 04
        //{"dataType":1,"errorCode":1,"glassWorkType":18,"imageCount":0,"otaStatus":0,"recordAudioDuration":0,"recordCount":0,"videoAngle":0,"videoCount":0,"videoDuration":0,"workTypeIng":0,"cmdType":0}
        try {
            dataType=data[7];
            if(dataType ==4){
                imageCount =(ByteUtil.bytesToInt(Arrays.copyOfRange(data, 8, 10)));
                videoCount =(ByteUtil.bytesToInt(Arrays.copyOfRange(data, 10, 12)));
                recordCount =(ByteUtil.bytesToInt(Arrays.copyOfRange(data, 12, 14)));
            }else if(dataType==1){
                //bc4105008c3d0201010002
                glassWorkType=data[8];
                if(glassWorkType==1 || glassWorkType==2|| glassWorkType==4||
                        glassWorkType==6|| glassWorkType==8  ||glassWorkType==7
                        ||glassWorkType==0x0b ||glassWorkType==0x0c  || glassWorkType==0x12){
                    if(data.length>9){
                        errorCode=data[9];
                        if(errorCode!=0){
                            // wifi name and password
                            Log.i(TAG,"暂时不处理");
                        }else {
                            workTypeIng=data[10];
                            Log.i(TAG,"workTypeIng->"+workTypeIng);
                        }
                    }
                }else  if(glassWorkType==5){
                    if(errorCode!=0){
                        otaStatus=errorCode;
                    }
                }
            }else if(dataType==3){
                p2pIp=ByteUtil.byteToInt(data[10])+"."+ByteUtil.byteToInt(data[11])+"."+ByteUtil.byteToInt(data[12])+"."+ByteUtil.byteToInt(data[12]);
            }else if(dataType ==2){
                videoAngle=data[8];
                videoDuration=(ByteUtil.bytesToInt(Arrays.copyOfRange(data, 9, 11)));
            }else if(dataType==6){
                recordAudioDuration=(ByteUtil.bytesToInt(Arrays.copyOfRange(data, 9, 11)));
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public int getRecordCount() {
        return recordCount;
    }


    public String getP2pIp() {
        return p2pIp;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getWorkTypeIng() {
        return workTypeIng;
    }

    public int getVideoAngle() {
        return videoAngle;
    }

    public int getVideoDuration() {
        return videoDuration;
    }

    public int getOtaStatus() {
        return otaStatus;
    }

    public int getRecordAudioDuration() {
        return recordAudioDuration;
    }
}
