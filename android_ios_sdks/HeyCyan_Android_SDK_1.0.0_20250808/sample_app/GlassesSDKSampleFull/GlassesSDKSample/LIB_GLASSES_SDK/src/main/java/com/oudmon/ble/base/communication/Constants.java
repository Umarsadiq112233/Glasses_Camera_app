package com.oudmon.ble.base.communication;

import java.util.UUID;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class Constants {

    public static final UUID UUID_SERVICE = UUID.fromString("6e40fff0-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID UUID_READ = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID UUID_WRITE = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID GATT_NOTIFY_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final int RSP_OK = 0x0;//	成功
    /**固件信息**/
    public final static UUID SERVICE_DEVICE_INFO = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
    public final static UUID CHAR_FIRMWARE_REVISION = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
    public final static UUID CHAR_HW_REVISION = UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB");
    public final static UUID CHAR_SOFTWARE_REVISION = UUID.fromString("00002A28-0000-1000-8000-00805F9B34FB");
    /**
     * 串口服务:	0x12, 0xA2, 0x4D, 0x2E, 0xFE, 0x14, 0x48, 0x8e, 0x93, 0xD2, 0x17, 0x3C
     */
    public static final UUID SERIAL_PORT_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7"); // 0000ffea-0000-1000-8000-00805f9b34fb");
    /**
     * 串口服务: Watch->App  0xC7, 0x5D, 0x2A, 0x01, 0xE3, 0x65, 0x26, 0xAF,0x47, 0x4E, 0x11, 0xD7, 0x28, 0xF7, 0x5B, 0xDE
     */
    public static final UUID SERIAL_PORT_CHARACTER_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");    // "0000ffeb-0000-1000-8000-00805f9b34fb");
    /**
     * 串口服务: App->Watch
     */
    public static final UUID SERIAL_PORT_CHARACTER_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7"); // 0000ffec-0000-1000-8000-00805f9b34fb");


    public static int CMD_DATA_LENGTH = 16;
    public static int FLAG_MASK_ERROR = 0x80;
    // 协议命令
    public static final byte CMD_SET_DEVICE_TIME = 0x01;
    //功能支持
    public static final byte CMD_DEVICE_FUNCTION_SUPPORT = 0x3C;
    public static final byte CMD_DEVICE_TOUCH = 0x3B;
    public static final byte CMD_DEVICE_REVISION = (byte) 0xa1;
    public static final byte CMD_TAKING_PICTURE = 0x02;
    public static final byte CMD_GET_DEVICE_ELECTRICITY_VALUE = 0x03;
    public static final byte CMD_SET_PHONE_OS = 0x04;
    public static final byte CMD_GET_STEP_TOTAL_SOMEDAY = 0x07;
    public static final byte CMD_SET_ALARM_CLOCK = 0x23;
    public static final byte CMD_GET_ALARM_CLOCK = 0x24;
    public static final byte CMD_SET_SIT_LONG = 0x25;
    public static final byte CMD_GET_SIT_LONG = 0x26;
    public static final byte CMD_SET_DRINK_TIME = 0x27;
    public static final byte CMD_GET_DRINK_TIME = 0x28;
    public static final byte CMD_GET_STEP_SOMEDAY_DETAIL = 0x43;
    public static final byte CMD_QUERY_DATA_DISTRIBUTION = 0x46;
    public static final byte CMD_GET_STEP_TODAY = 0x48;
    public static final byte CMD_ANTI_LOST_RATE = 0x50;
    public static final byte CMD_GET_TIME_SETTING = 0x0A;
    public static final byte CMD_SET_ANCS_ON_OFF = 0x60;
    public static final byte CMD_GET_ANCS_ON_OFF = 0x61;
    public static final byte CMD_START_HEART_RATE = 0x69;
    public static final byte CMD_STOP_HEART_RATE = 0x6A;
    public static final byte CMD_HEALTH_ECG_START = 0x6C;
    public static final byte CMD_HEALTH_ECG_DATA = 0x6D;
    public static final byte CMD_HEALTH_PPG_DATA = 0x6E;
    public static final byte CMD_ECG_STATUS_DATA = 0x6F;
    public static final byte CMD_ECG_MEASURE_TIME = 0x70;

    public static final byte CMD_TUNE_TIME_DIRECT = 0x73;
    public static final byte CMD_TUNE_TIME_INVERSE = 0x74;
    public static final byte CMD_DEVICE_DIAL_INDEX = (byte) 0x75;
    public static final byte CMD_DEVICE_BATTERY_SAVING = (byte) 0x76;
    public static final byte CMD_MSG_NOTIFY = 0x72;
    public static final byte CMD_MSG_GET_HW_AND_FW_VERSION = (byte) 0x93;

    public static final byte CMD_PUSH_MSG = (byte) 0x72;     //消息推送
    public static final byte CMD_FANWAN = (byte) 0x05;     //翻腕亮屏
    public static final byte CMD_MUTE = (byte) 0x06;     //勿扰模式
    public static final byte CMD_INTELL = (byte) 0x09;     //防丢模式
    public static final byte CMD_GET_SLEEP = (byte) 0x44;     //获取睡眠时间

    public static final byte CMD_RE_BOOT = (byte) 0x08;
    public static final byte CMD_RE_STORE = (byte) 0xFF;
    public static final byte CMD_BIND_SUCCESS = (byte) 0x10;
    public static final byte CMD_DISPLAY_CLOCK = (byte) 0x12;
    public static final byte CMD_DISPLAY_STYLE = (byte) 0x2A;

    public static final byte CMD_BP_TIMING_MONITOR_SWITCH = (byte) 0x0C;  //定时测量血压开关
    public static final byte CMD_BP_TIMING_MONITOR_DATA = (byte) 0x0D;  //定时测量血压数据
    public static final byte CMD_BP_TIMING_MONITOR_CONFIRM = (byte) 0x0E;  //定时血压确认回复

    public static final byte CMD_HR_TIMING_MONITOR_SWITCH = (byte) 0x16;
    public static final byte CMD_HR_TIMING_MONITOR_DATA = (byte) 0x0D;
    public static final byte CMD_HR_TIMING_MONITOR_CONFIRM = (byte) 0x0E;

    public static final byte CMD_ORIENTATION = (byte) 0x29;

    public static final byte CMD_PHONE_NOTIFY = (byte) 0x11;

    public static final byte CMD_GET_SPORT = (byte) 0x13;
    public static final byte CMD_GET_BAND_PRESSURE = (byte) 0x14;
    public static final byte CMD_CALIBRATION_RATE = (byte) 0x20;

    public static final byte CMD_GET_HEART_RATE = (byte) 0x15;
    public static final byte CMD_GET_PERSONALIZATION_SETTING = (byte) 0x17;

    public static final byte CMD_GET_DEGREE_SWITCH = (byte) 0x19;
    public static final byte CMD_SEND_WEATHER_FORECAST = (byte) 0x1A;
    public static final byte CMD_GET_BRIGHTNESS = (byte) 0x1B;
    public static final byte CMD_GET_MUSIC_SWITCH = (byte) 0x1C;
    public static final byte CMD_MUSIC_COMMAND = (byte) 0x1D;
    public static final byte CMD_DISPLAY_TIME = (byte) 0x1F;
    public static final byte CMD_GPS_ONLINE = (byte) 0x54;
    public static final byte CMD_PACKAGE_LENGTH = (byte) 0x2F;
    public static final byte CMD_MENSTRUATION = (byte) 0x2B;

    public static final byte CMD_TARGET_SETTING = (byte) 0x21;
    public static final byte CMD_FIND_THE_PHONE = (byte) 0x22;
    public static final byte CMD_DEVICE_AVATAR= (byte) 0x32;

    public static final byte CMD_AGPS_SWITCH = (byte) 0x30;
    public static final byte CMD_DEVICE_NOTIFY = (byte) 0x73;
    public static final byte CMD_PHONE_GPS = (byte) 0x74;
    public static final byte CMD_PHONE_SPORT = (byte) 0x77;
    public static final byte CMD_PHONE_SPORT_N0TIFY = (byte) 0x78;
    public static final byte CMD_MUSLIM_DATA = (byte) 0x7A;
    public static final byte CMD_MUSLIM_GOAL_DATA = (byte) 0x7B;

    public static final byte Intell_time = 0x05;
    public static final int STRING_LIMITE = 64;

    public static final int BAND_PRESSURE_COUNT = 50;
    public static final byte TO_OTA = (byte) 0x0F;  //切换单页OTA

    public static final byte CMD_TEST_OPEN= (byte) 0xC9;
    public static final byte CMD_TEST_CLOSE= (byte) 0xCA;
    public static final byte CMD_AUTO_BLOOD_OXYGEN= (byte) 0x2C;
    public static final byte CMD_REAL_TIME_HEART_RATE= (byte) 0x1e;
    public static final byte CMD_BlackList_LOCATION= (byte) 0x2d;

    public static final byte CMD_PRESSURE_SETTING= (byte) 0x36;
    public static final byte CMD_PRESSURE= (byte) 0x37;
    public static final byte CMD_HRV_ENABLE= (byte) 0x38;
    public static final byte CMD_HRV= (byte) 0x39;

    public static final byte CMD_DEVICE_SUGAR_LIPIDS = (byte) 0x3A;
    public static final byte CMD_DEVICE_GLASS_MODEL_CONTROL = (byte) 0x3E;


}
