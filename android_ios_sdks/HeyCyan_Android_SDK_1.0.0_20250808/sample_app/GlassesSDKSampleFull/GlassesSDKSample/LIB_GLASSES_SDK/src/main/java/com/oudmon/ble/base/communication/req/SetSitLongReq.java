package com.oudmon.ble.base.communication.req;
import android.util.Log;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.entity.StartEndTimeEntity;
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.17 设置久坐提醒
 */

public class SetSitLongReq extends BaseReqCmd {

    private byte[] data;

    /**
     * @param startEndTimeEntity s
     * @param weekMask           w
     * @param cycle              单位为分钟，取值为30、60、90。如90表示90分钟提醒一次。
     */
    public SetSitLongReq(StartEndTimeEntity startEndTimeEntity, byte weekMask, int cycle) {
        super(Constants.CMD_SET_SIT_LONG);
        if (cycle != 30 && cycle != 60 && cycle != 90) {
            Log.i(TAG, "时间周期参数错误，已调整为正常的60s，原参数为: " + cycle);  //某些时候该值为非正常而导致崩溃
            cycle = 60;
            //throw new IllegalArgumentException("时间周期参数错误 ");
        }
        data = new byte[] {
                BLEDataFormatUtils.decimalToBCD(startEndTimeEntity.getStartHour()),
                BLEDataFormatUtils.decimalToBCD(startEndTimeEntity.getStartMinute()),
                BLEDataFormatUtils.decimalToBCD(startEndTimeEntity.getEndHour()),
                BLEDataFormatUtils.decimalToBCD(startEndTimeEntity.getEndMinute()),
                weekMask,
                (byte) cycle
        };
    }

    @Override
    protected byte[] getSubData() {
        return data;
    }
}
