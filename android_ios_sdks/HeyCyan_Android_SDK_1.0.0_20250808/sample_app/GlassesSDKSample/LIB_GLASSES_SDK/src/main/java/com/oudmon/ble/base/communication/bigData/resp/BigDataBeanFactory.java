package com.oudmon.ble.base.communication.bigData.resp;

import com.oudmon.ble.base.communication.LargeDataHandler;

public class BigDataBeanFactory {
    public static BaseResponse createBean(int key) {
        BaseResponse cmd = null;
        switch (key) {
            case LargeDataHandler.ACTION_GLASSES_CONTROL:
                cmd = new GlassModelControlResponse();
                break;
            case LargeDataHandler.ACTION_BT_MAC_Protocol:
                cmd = new ClassBluetoothResponse();
                break;
            case LargeDataHandler.ACTION_SYNC_TIME:
                cmd = new SyncTimeResponse();
                break;
            case LargeDataHandler.ACTION_PICTURE_THUMBNAILS:
                cmd = new PictureThumbnailsResponse();
                break;
            case LargeDataHandler.ACTION_DEVICE_DATA_REPORTING:
                cmd = new GlassesDeviceNotifyRsp();
                break;
            case LargeDataHandler.ACTION_GPT_UPLOAD:
                cmd = new AiChatResponse();
                break;
            case LargeDataHandler.ACTION_GLASSES_BATTERY:
                cmd = new BatteryResponse();
                break;
            case LargeDataHandler.ACTION_DEVICE_INFO:
                cmd = new DeviceInfoResponse();
                break;
            case LargeDataHandler.ACTION_DEVICE_AI_VOICE:
                cmd = new GlassesAiVoiceRsp();
                break;
            case LargeDataHandler.ACTION_DEVICE_WEAR:
                cmd = new GlassesWearRsp();
                break;
            case LargeDataHandler.ACTION_VOICE_STATUS:
                cmd = new GlassesAiVoicePlayStatusRsp();
                break;
            case LargeDataHandler.ACTION_DEVICE_WEAR_SUPPORT:
                cmd = new GlassesTouchSupportRsp();
                break;
            case LargeDataHandler.ACTION_VOLUME_CONTROL:
                cmd = new VolumeControlResponse();
                break;
            default:

                break;
        }
        return cmd;
    }
}
