package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * @author gs ,
 * @date /3/1
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class MenstruationReq extends MixtureReq {

    public MenstruationReq(byte key) {
        super(Constants.CMD_MENSTRUATION);
        subData = new byte[]{0x01};
    }

    public MenstruationReq(boolean isEnable, int during, int cycle, int lastStart, int lastEnd, boolean alarmEnable, int mAlert, int oAlert, int hour, int min) {
        super(Constants.CMD_MENSTRUATION);
        subData = new byte[]{0x02,
                (byte) (isEnable ? 0x01 : 0x00),
                (byte) (during & 0xff),
                (byte) (cycle & 0xff),
                (byte) (lastStart & 0xff),
                (byte) (lastEnd & 0xff),
                (byte) (alarmEnable ? 0x01:0x00),
                (byte) (mAlert & 0xff),
                (byte) (oAlert & 0xff),
                (byte) (hour & 0xff),
                (byte) (min & 0xff)};
    }
}
