package com.oudmon.ble.base.communication.rsp;

import android.util.Log;

import com.oudmon.qc_utils.bytes.DataTransferUtils;

/**
 * Created by Jxr35 on 2018/3/28
 */

public class WeatherForecastRsp extends MixtureRsp {

    private boolean isSuccess = false;

    @Override
    protected void readSubData(byte[] subData) {
        Log.i(TAG, "WeatherForecastRsp.. readSubData: " + DataTransferUtils.getHexString(subData));
        isSuccess = subData[0] == 0x1a;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public String toString() {
        return "WeatherForecastRsp{" +
                "isSuccess=" + isSuccess +
                ", status=" + status +
                '}';
    }
}
