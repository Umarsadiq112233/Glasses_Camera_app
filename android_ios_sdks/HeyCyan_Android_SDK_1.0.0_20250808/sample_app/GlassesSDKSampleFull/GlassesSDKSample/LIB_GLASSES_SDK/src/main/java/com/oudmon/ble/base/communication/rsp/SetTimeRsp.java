package com.oudmon.ble.base.communication.rsp;

import com.oudmon.ble.base.communication.utils.ByteUtil;

import java.util.Arrays;

/**
 * Created by Jxr35 on swatch_device_text5/9
 */
public class SetTimeRsp extends BaseRspCmd {

    public boolean mSupportTemperature;
    public boolean mSupportPlate;
    public boolean mSupportMenstruation;
    public boolean mSupportCustomWallpaper;
    public boolean mSupportBloodOxygen;
    public boolean mSupportBloodPressure;
    public boolean mSupportFeature;
    public boolean mSupportOneKeyCheck;
    public boolean mSupportWeather;
    public boolean mNewSleepProtocol;
    public int mMaxWatchFace;
    public boolean mSupportContact;
    public boolean mSupportManualHeart;
    public boolean mSupportECard;
    public boolean mSupportLocation;
    public int mMaxContacts;
    public boolean mMusicSupport;
    //8763e mcu
    public boolean rtkMcu;
    public boolean mEbookSupport;
    public boolean mSupportWeChat;
    public boolean mSupportAvatar;

    public boolean mSupportBloodSugar;
    public int width;
    public int height;

    public boolean mSupportLyrics;
    public boolean mSupportAlbum;
    public boolean mSupportGPS;
    public boolean mSupportJieLiMusic;
    public boolean mSupport4G;
    public boolean bpSettingSupport;

    public boolean mSupportNavPicture;
    public boolean mSupportPressure;
    public boolean mSupportHrv;
    public boolean mSupportManualBloodOxygen;
    public boolean mSupportAppMeasure;
    public boolean mYaWeiSupport;

    public boolean mSupportRecord;

    @Override
    public boolean acceptData(byte[] data) {
        mSupportTemperature = data[0] == 1;
        mSupportPlate = data[1] == 1;
//        mSupportMenstruation = data[2] == 1;
        mSupportMenstruation = data[2] == 1;
        mSupportCustomWallpaper = (data[3] & 0x01)!= 0;
        mSupportBloodOxygen = (data[3] & 0x02)!= 0;
        mSupportBloodPressure=(data[3] & 0x04)!= 0;
        mSupportFeature=(data[3] & 0x08)!= 0;
        mSupportOneKeyCheck=(data[3] & 0x10)!= 0;
        mSupportWeather=(data[3] & 0x20)!= 0;
        mSupportWeChat=(data[3] & 0x40) == 0;
        mSupportAvatar=(data[3] & 0x80) != 0;

        width= ByteUtil.bytesToInt(Arrays.copyOfRange(data, 4, 6));
        height= ByteUtil.bytesToInt(Arrays.copyOfRange(data, 6, 8));

        mSupportAppMeasure=(data[10] & 0x20) !=0;
        mSupportManualBloodOxygen=(data[10] & 0x40) !=0;
        mYaWeiSupport=(data[10] & 0x80) != 0;

        mNewSleepProtocol=data[8] ==1;
        mMaxWatchFace=data[9];

        mSupportContact=(data[10] & 0x01) !=0;
        mSupportLyrics=(data[10] & 0x02) !=0;
        mSupportAlbum=(data[10] & 0x04) !=0;
        mSupportGPS=(data[10] & 0x08) !=0;
        mSupportJieLiMusic=(data[10] & 0x10) !=0;

        mSupportManualHeart=(data[11] & 0x01) !=0;
        mSupportECard=(data[11] & 0x02)!=0;
        mSupportLocation=(data[11] & 0x04)!=0;

        mMusicSupport=(data[11] & 0x10)!=0;
        rtkMcu=(data[11] & 0x20)!=0;
        mEbookSupport=(data[11] & 0x40)!=0;
        mSupportBloodSugar=(data[11] & 0x80) != 0;
//        mSupportBloodSugar=true;

        if(data[12]==0){
            mMaxContacts=20;
        }else {
            mMaxContacts=(data[12])*10;
        }

        bpSettingSupport=(data[13] & 0x02)!=0;
        mSupport4G=(data[13] & 0x04)!=0;
        mSupportNavPicture=(data[13] & 0x08)!=0;
        mSupportPressure=(data[13] & 0x10)!=0;
        mSupportHrv=(data[13] & 0x20)!=0;
//        mSupportHrv=true;
//        mSupportPressure=true;
        mSupportRecord=(data[13] & 0x01) !=0;
        return false;
    }

}
