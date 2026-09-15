package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class StartHeartRateReq extends BaseReqCmd {

    public static final byte TYPE_HEARTRATE = 0x01;//: 表示心率(下位机测量心率)
    public static final byte TYPE_BLOODPRESSURE = 0x02;//: 表示血压(下位机测量心率)
    public static final byte TYPE_BLOODOXYGEN = 0x03;//: 表示血氧(下位机测量心率)(暂定)
    public static final byte TYPE_FATIGUE = 0x04;//: 表示疲劳度(下位机测量心率)(暂定)
    public static final byte TYPE_HEALTHCHECK = 0x05;//: 表示一键测量(下位机测量心率)(暂定)
    public static final byte TYPE_REALTIMEHEARTRATE = 0x06;//: 表示实时心率测量(下位机测量心率)
    public static final byte TYPE_ECG = 0x07;   //: 表示ECG测量(下位机测量ECG)
    public static final byte TYPE_PRESSURE = 0x08;   //: 压力
    public static final byte TYPE_BLOOD_SUGAR= 0x09;   //: 血糖
    public static final byte TYPE_HRV= 0x0A;   //: HRV
    public static final byte TYPE_BODY_TEMPERATURE= 0x0B;   //: 体温
    private byte type;
    public static final byte ACTION_START = 0x01;//: 启动测量
    public static final byte ACTION_PAUSE = 0x02;//: 暂停测量
    public static final byte ACTION_CONTINUE = 0x03;//: 继续测量
    public static final byte ACTION_STOP = 0x04;//: 结束测量
    private byte sub;

    private StartHeartRateReq(byte type, byte sub) {
        super(Constants.CMD_START_HEART_RATE);
        this.type = type;
        this.sub = sub;
    }

    /**
     * TYPE_HEARTRATE = 0x01;//: 表示心率(下位机测量心率)
     * TYPE_BLOODPRESSURE = 0x02;//: 表示血压(下位机测量心率)
     * TYPE_BLOODOXYGEN = 0x03;//: 表示血氧(下位机测量心率)(暂定)
     * TYPE_FATIGUE = 0x04;//: 表示疲劳度(下位机测量心率)(暂定)
     * TYPE_HEALTHCHECK = 0x05;//: 表示一键测量(下位机测量心率)(暂定)
     *
     * @param type
     * @return
     */
    public static StartHeartRateReq getSimpleReq(byte type) {
        return new StartHeartRateReq(type, type < 0x03 ? 0x00 : BLEDataFormatUtils.decimalToBCD(25));
    }

    /**
     * 表示实时心率测量(下位机测量心率)
     *
     * @param action action_start = 0x01;//: 启动测量
     *               action_pause = 0x02;//: 暂停测量
     *               action_continue = 0x03;//: 继续测量
     *               action_stop = 0x04;//: 结束测量
     * @return
     */
    public static StartHeartRateReq getRealtimeHeartRate(byte action) {
        return new StartHeartRateReq(TYPE_REALTIMEHEARTRATE, action);
    }

    @Override
    protected byte[] getSubData() {
        return new byte[] {type, sub};
    }
}
