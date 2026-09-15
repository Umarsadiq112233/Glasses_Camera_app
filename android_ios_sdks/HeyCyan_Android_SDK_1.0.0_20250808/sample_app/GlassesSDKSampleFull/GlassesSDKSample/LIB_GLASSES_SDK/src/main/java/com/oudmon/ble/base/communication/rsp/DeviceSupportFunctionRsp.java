package com.oudmon.ble.base.communication.rsp;


public class DeviceSupportFunctionRsp extends BaseRspCmd {
    public boolean supportTouch;
    public boolean supportMoslin;
    public boolean supportAPPRevision;
    public boolean supportBlePair;
    public boolean supportGesture;

    public boolean supportRingMusic;
    public boolean supportRingVideo;
    public boolean supportRingEbook;
    public boolean supportRingCamera;
    public boolean supportRingPhoneCall;
    public boolean supportRingGame;
    public boolean supportHeart;

    public boolean supportLongSit;
    public boolean supportDrink;



    @Override
    public boolean acceptData(byte[] data) {

        supportTouch = (data[1] & 0x01) != 0;
        supportMoslin = (data[1] & 0x02)!= 0;

        supportAPPRevision=(data[1] & 0x04)!= 0;
        supportBlePair=(data[1] & 0x08)!= 0;

        supportGesture=(data[1] & 0x80)!= 0;

        supportRingMusic =(data[2] & 0x01) != 0;
        supportRingVideo =(data[2] & 0x02) != 0;
        supportRingEbook =(data[2] & 0x04) != 0;
        supportRingCamera =(data[2] & 0x08) != 0;
        supportRingPhoneCall =(data[2] & 0x10) != 0;
        supportRingGame =(data[2] & 0x20) != 0;
        supportHeart =(data[2] & 0x40) != 0;

        supportLongSit =(data[4] & 0x04) != 0;
        supportDrink =(data[4] & 0x08) != 0;

//        mSupportOneKeyCheck=(data[3] & 0x10)!= 0;
//        mSupportWeather=(data[3] & 0x20)!= 0;
//        mSupportWeChat=(data[3] & 0x40) == 0;
//        mSupportAvatar=(data[3] & 0x80) != 0;

        return false;
    }

}
