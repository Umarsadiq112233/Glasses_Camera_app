package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class StopHeartRateReq extends BaseReqCmd {
    private byte[] data;

    private StopHeartRateReq(byte type, byte bb, byte cc) {
        super(Constants.CMD_STOP_HEART_RATE);
        data = new byte[] {type, bb, cc};
    }

    public static StopHeartRateReq stopHeartRate(byte hrValue) {
        return new StopHeartRateReq((byte) 0x01, hrValue, (byte) 0x00);
    }

    public static StopHeartRateReq stopBloodPressure(byte sbp, byte dbp) {
        return new StopHeartRateReq((byte) 0x02, sbp, dbp);
    }

    public static StopHeartRateReq stopBloodOxygen(byte oxyValue) {
        return new StopHeartRateReq((byte) 0x03, oxyValue, (byte) 0x00);
    }


    public static StopHeartRateReq stopFatigue(byte fatigueScore) {
        return new StopHeartRateReq((byte) 0x04, fatigueScore, (byte) 0x00);
    }


    public static StopHeartRateReq stopHealthCheck() {
        return new StopHeartRateReq((byte) 0x05, (byte) 0x00, (byte) 0x00);
    }


    public static StopHeartRateReq stopTemperatureCheck() {
        return new StopHeartRateReq((byte) 0x0b, (byte) 0x00, (byte) 0x00);
    }

    /**
     * 心电分析结果，需要返回给手环
     * @param ecgType ecg结果，0表示一般，1表示良好
     * @return resp
     */
    public static StopHeartRateReq stopEcg(int ecgType) {
        return new StopHeartRateReq((byte) 0x07, (byte) ecgType, (byte) 0x00);
    }

    public static StopHeartRateReq stopPressure(byte value) {
        return new StopHeartRateReq((byte) 0x08, value, (byte) 0x00);
    }


    public static StopHeartRateReq stopBloodSugar(byte value) {
        return new StopHeartRateReq((byte) 0x09, value, (byte) 0x00);
    }

    public static StopHeartRateReq stopHrv(byte value) {
        return new StopHeartRateReq((byte) 0x0A, value, (byte) 0x00);
    }


    @Override
    protected byte[] getSubData() {
        return data;
    }
}
