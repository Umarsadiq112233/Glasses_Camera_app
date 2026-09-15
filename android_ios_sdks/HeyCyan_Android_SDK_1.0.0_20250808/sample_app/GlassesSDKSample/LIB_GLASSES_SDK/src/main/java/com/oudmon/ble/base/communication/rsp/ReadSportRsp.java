package com.oudmon.ble.base.communication.rsp;

import android.util.Log;

import com.oudmon.ble.base.communication.entity.BleSport;
import com.oudmon.ble.base.communication.utils.DataParseUtils;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class ReadSportRsp extends BaseRspCmd {

    private int size = 0;
    private int index = 0;
    private byte[] valueData;
    private BleSport sport = new BleSport();
    private boolean endFlag = false;

    @Override
    public boolean acceptData(byte[] data) {
        //总长度18字节
//        Log.i(TAG, "acceptData: data=" + DataTransferUtils.getHexString(data));
        byte flag = data[0];
        if ((flag & 0xff) == 0xff) {
            endFlag = true;
            return false;
        }
        if ((flag & 0xff) == 0x00) {//报告数据大小
            endFlag = false;
            size = data[1];
            valueData = new byte[size * 13];
            Log.i(TAG, "0x00.. size: " + size + ", valueData: " + valueData);
        } else {//数据
            for (int i = 1; i < data.length; i++) {
                valueData[index + i - 1] = data[i];
            }
            Log.e(TAG, "valueData = " + DataTransferUtils.getHexString(valueData));

            Log.i(TAG, "0x00.. size: " + size + ", valueData: " + valueData);

            index = index + 13;

            if (flag == size - 1) {
                //最后一条
                byte[] sub = new byte[4];
                int rateCount = 0;

                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < 4; j++) {
                        sub[j] = valueData[i * 4 + j];
                    }
                    switch (i) {
                        case 0:
                            sport.setStartTime(DataParseUtils.byteArrayToInt(sub));
                            break;
                        case 1:
                            sport.setDuration(DataParseUtils.byteArrayToInt(sub));
                            break;
                        case 2:
                            sport.setSportType(DataParseUtils.byteArrayToInt(sub));
                            break;
                        case 3:
                            sport.setStepCount(DataParseUtils.byteArrayToInt(sub));
                            break;
                        case 4:
                            sport.setDistance(DataParseUtils.byteArrayToInt(sub));
                            break;
                        case 5:
                            sport.setCalories(DataParseUtils.byteArrayToInt(sub));
                            break;
                        case 6:
                            rateCount = DataParseUtils.byteArrayToInt(sub);
                            break;
                    }
                }

                Log.e(TAG, "rateCount = " + rateCount);
                int[] rateArray = new int[rateCount];
                int index = 0;
                for (int i = 28; i < rateCount + 28; i++) {
                    int value = valueData[i] & 0xff;
                    rateArray[index] = value;
                    index++;
                }
                sport.setRateValue(rateArray);

                return false;
            }
        }
        return true;
    }

    public BleSport getBleSport() {
        return sport;
    }

    public boolean isEndFlag() {
        return endFlag;
    }
}
