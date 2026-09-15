package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.entity.StartEndTimeEntity;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class BpSettingReq extends MixtureReq {

    private BpSettingReq() {
        super(Constants.CMD_BP_TIMING_MONITOR_SWITCH);
        subData = new byte[]{0x01};
    }

    /**
     * @param isEnable 是否打开，false 会忽略后面的参数
     * @param startEndTimeEntity s
     * @param multiple 采样间隔，仅支持30分钟的整数倍
     */
    private BpSettingReq(boolean isEnable, StartEndTimeEntity startEndTimeEntity, int multiple) {
        super(Constants.CMD_BP_TIMING_MONITOR_SWITCH);
        subData = new byte[]{0x02,
                (byte) (isEnable ? 0x01 : 0x00),
                (byte) (startEndTimeEntity.getStartHour() & 0xff),
                (byte) (startEndTimeEntity.getStartMinute() & 0xff),
                (byte) (startEndTimeEntity.getEndHour() & 0xff),
                (byte) (startEndTimeEntity.getEndMinute() & 0xff),
                (byte) (multiple & 0xff)};
    }

    public static BpSettingReq getReadInstance(){
        return new BpSettingReq();
    }

    public static BpSettingReq getWriteInstance(boolean isEnable, StartEndTimeEntity startEndTimeEntity, int multiple) {
        return new BpSettingReq(isEnable, startEndTimeEntity, multiple);
    }
}
