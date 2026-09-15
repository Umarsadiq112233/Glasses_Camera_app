package com.oudmon.ble.base.bluetooth;
import com.oudmon.ble.base.communication.rsp.AppRevisionResp;
import com.oudmon.ble.base.communication.rsp.AppSportRsp;
import com.oudmon.ble.base.communication.rsp.BaseRspCmd;
import com.oudmon.ble.base.communication.rsp.BatteryRsp;
import com.oudmon.ble.base.communication.rsp.BloodOxygenSettingRsp;
import com.oudmon.ble.base.communication.rsp.BloodSugarLipidsSettingRsp;
import com.oudmon.ble.base.communication.rsp.BpDataRsp;
import com.oudmon.ble.base.communication.rsp.BpSettingRsp;
import com.oudmon.ble.base.communication.rsp.CameraNotifyRsp;
import com.oudmon.ble.base.communication.rsp.DegreeSwitchRsp;
import com.oudmon.ble.base.communication.rsp.DeviceAvatarRsp;
import com.oudmon.ble.base.communication.rsp.DeviceNotifyRsp;
import com.oudmon.ble.base.communication.rsp.DeviceSupportFunctionRsp;
import com.oudmon.ble.base.communication.rsp.DisplayTimeRsp;
import com.oudmon.ble.base.communication.rsp.DndRsp;
import com.oudmon.ble.base.communication.rsp.FindPhoneRsp;
import com.oudmon.ble.base.communication.rsp.GlassModelControlResp;
import com.oudmon.ble.base.communication.rsp.HRVRsp;
import com.oudmon.ble.base.communication.rsp.HRVSettingRsp;
import com.oudmon.ble.base.communication.rsp.HeartRateSettingRsp;
import com.oudmon.ble.base.communication.rsp.MusicCommandRsp;
import com.oudmon.ble.base.communication.rsp.MuslimRsp;
import com.oudmon.ble.base.communication.rsp.MuslimTargetRsp;
import com.oudmon.ble.base.communication.rsp.PackageLengthRsp;
import com.oudmon.ble.base.communication.rsp.PalmScreenRsp;
import com.oudmon.ble.base.communication.rsp.PhoneNotifyRsp;
import com.oudmon.ble.base.communication.rsp.PressureRsp;
import com.oudmon.ble.base.communication.rsp.PressureSettingRsp;
import com.oudmon.ble.base.communication.rsp.ReadAlarmRsp;
import com.oudmon.ble.base.communication.rsp.ReadBlePressureRsp;
import com.oudmon.ble.base.communication.rsp.ReadDetailSportDataRsp;
import com.oudmon.ble.base.communication.rsp.ReadHeartRateRsp;
import com.oudmon.ble.base.communication.rsp.ReadMessagePushRsp;
import com.oudmon.ble.base.communication.rsp.ReadSitLongRsp;
import com.oudmon.ble.base.communication.rsp.ReadSleepDetailsRsp;
import com.oudmon.ble.base.communication.rsp.RealTimeHeartRateRsp;
import com.oudmon.ble.base.communication.rsp.SetTimeRsp;
import com.oudmon.ble.base.communication.rsp.SimpleStatusRsp;
import com.oudmon.ble.base.communication.rsp.StartHeartRateRsp;
import com.oudmon.ble.base.communication.rsp.TargetSettingRsp;
import com.oudmon.ble.base.communication.rsp.TimeFormatRsp;
import com.oudmon.ble.base.communication.rsp.TodaySportDataRsp;
import com.oudmon.ble.base.communication.rsp.TouchControlResp;
import com.oudmon.ble.base.communication.rsp.WeatherForecastRsp;

/**
 * @Author: Hzy
 * @CreateDate: 2022/3/14 15:16
 * <p>
 * "程序应该是写给其他人读的,
 * 让机器来运行它只是一个附带功能"
 */
public class BeanFactory {
    public static BaseRspCmd createBean(int key,int type){
        BaseRspCmd cmd=null;
        switch (key){
            case 0x01:
                cmd=new SetTimeRsp();
                break;
            case 0x02:
                cmd=new CameraNotifyRsp();
                break;
            case 0x03:
                cmd=new BatteryRsp();
                break;
            case 0x04:
            case 0x07:
            case 0x08:
            case 0x09:
            case 0x0e:

                break;
            case 0x05:
                cmd=new PalmScreenRsp();
                break;
            case 0x06:
                cmd=new DndRsp();
                break;
            case 0x0A:
                cmd=new TimeFormatRsp();
                break;
            case 0x0C:
                cmd=new BpSettingRsp();
                break;
            case 0x0D:
                cmd=new BpDataRsp();
                break;
            case 0x19:
                cmd=new DegreeSwitchRsp();
                break;
            case 0x1f:
                cmd=new DisplayTimeRsp();
                break;
            case 0x21:
                cmd=new TargetSettingRsp();
                break;
            case 0x22:
                cmd=new FindPhoneRsp();
                break;
            case 0x32:
                cmd=new DeviceAvatarRsp();
                break;
            case 0x3c:
                cmd=new DeviceSupportFunctionRsp();
                break;
            case 0x36:
                cmd=new PressureSettingRsp();
                break;
            case 0x37:
                cmd=new PressureRsp();
                break;
            case 0x38:
                cmd=new HRVSettingRsp();
                break;
            case 0x39:
                cmd=new HRVRsp();
                break;
            case 0x16:
                cmd=new HeartRateSettingRsp();
                break;
            case 0x1d:
                cmd=new MusicCommandRsp();
                break;
            case 0x2f:
                cmd= new PackageLengthRsp();
                break;
            case 0x11:
                cmd= new PhoneNotifyRsp();
                break;
            case 0x28:
                cmd=new ReadAlarmRsp();
                break;
            case 0x14:
                cmd=new ReadBlePressureRsp();
                break;
            case 0x43:
                cmd=new ReadDetailSportDataRsp();
                break;
            case 0x15:
                cmd=new ReadHeartRateRsp();
                break;
            case 0x26:
                cmd=new ReadSitLongRsp();
                break;
            case 0x44:
                cmd=new ReadSleepDetailsRsp();
                break;
            case 0x1e:
                cmd=new RealTimeHeartRateRsp();
                break;
            case 0x72:
                cmd=new SimpleStatusRsp();
                break;
            case 0x61:
                cmd=new ReadMessagePushRsp();
                break;
            case 0x69:
                cmd=new StartHeartRateRsp();
                break;
            case 0x48:
                cmd=new TodaySportDataRsp();
                break;
            case 0x1a:
                cmd= new WeatherForecastRsp();
                break;
            case 0x2c:
                cmd=new BloodOxygenSettingRsp();
                break;
            case 0x73:
            case 0x78:
                cmd=new DeviceNotifyRsp();
                break;
            case 0x7A:
                cmd=new MuslimRsp();
                break;
            case 0x77:
                cmd=new AppSportRsp();
                break;
            case 0x3b:
                cmd=new TouchControlResp();
                break;
            case 0x3e:
                cmd=new GlassModelControlResp();
                break;
            case -223:
                cmd=new AppRevisionResp();
                break;
            case 0x3A:
                cmd=new BloodSugarLipidsSettingRsp();
                break;
            case 0x7B:
                cmd=new MuslimTargetRsp();
                break;
        }
        return cmd;
    }
}
