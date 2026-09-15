package com.oudmon.ble.base.communication.rsp;

import android.util.Log;

import com.oudmon.qc_utils.bytes.DataTransferUtils;
import com.oudmon.qc_utils.date.DateUtil;

public class PressureRsp extends BaseRspCmd {
    private int size = 0;
    private int index = 0;
    private byte[] pressureArray;
    private boolean endFlag = false;
    private int range=30;
    private DateUtil today;
    private int offset=-1;

    @Override
    public boolean acceptData(byte[] data) {
//         Log.i(TAG,ByteUtil.byteArrayToString(data));
        //总长度18字节
        try {
            byte flag = data[0];
            if ((flag & 0xff) == 0xff) {
                endFlag = true;
                return false;
            }
            if ((flag & 0xff) == 0x00) {
                endFlag = false;
                size = data[1];
                range=data[2];
                pressureArray = new byte[size * 13];
                Log.i(TAG, "0x00.. size: " + size);
            } else if ((flag & 0xff) == 0x01) {
                int day = data[1];
                offset=day;
                today=new DateUtil();
                today.addDay(-day);
                System.arraycopy(data, 2, pressureArray, 0, data.length - 2);
                index += data.length - 2;
            } else {
                System.arraycopy(data, 1, pressureArray, index, data.length - 1);
                index += 13;

                if (flag == size - 1) {
                    endFlag = true;
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    public int getOffset() {
        return offset;
    }

    public boolean isEndFlag() {
        return endFlag;
    }

    public byte[] getPressureArray() {
        if(pressureArray==null){
            pressureArray=new byte[]{};
        }
        return pressureArray;
    }

    public int getRange() {
        return range;
    }

    public DateUtil getToday() {
        return today;
    }
}
