package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 使用这个的时候需要自己区分是自己req的 rsp（action=0），还是手环主动请求的notify（action=1,2,3）
 */

public class CameraNotifyRsp extends BaseRspCmd {

    public static final int ACTION_INTO_CAMERA_UI = 0x01; //下位机(手环)向上位机(手机)发起切换到拍照界面请求
    public static final int ACTION_TAKE_PHOTO = 0x02;// 下位机(手环)向上位机(手机)发起拍照请求
    public static final int ACTION_FINISH = 0x03;// 下位机(手环)向上位机(手机)发起结束拍照请求

    private int action = 0;

    @Override
    public boolean acceptData(byte[] data) {
        action = data[0];
        return false;
    }

    public int getAction() {
        return action;
    }
}
