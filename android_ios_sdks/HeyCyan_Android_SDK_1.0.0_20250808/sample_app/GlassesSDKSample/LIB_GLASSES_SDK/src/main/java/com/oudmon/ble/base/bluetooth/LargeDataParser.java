package com.oudmon.ble.base.bluetooth;
import com.oudmon.ble.base.communication.ILargeDataResponse;
import com.oudmon.ble.base.communication.LargeDataHandler;
import com.oudmon.ble.base.communication.bigData.resp.BaseResponse;
import com.oudmon.ble.base.communication.bigData.resp.BigDataBeanFactory;
import com.oudmon.ble.base.communication.utils.ByteUtil;

import java.util.Arrays;

/**
 * @author gs ,
 * @date /5/12
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class LargeDataParser {
    private static LargeDataParser instance;
    public static String uuid_notify = "de5bf729-d711-4e47-af26-65e3012a5dc7";
    public byte[] tempData;
    public int dataLength = 0;

    private LargeDataParser() {

    }

    public static LargeDataParser getInstance() {
        if (instance == null) {
            synchronized (LargeDataParser.class) {
                if (instance == null) {
                    instance = new LargeDataParser();
                }
            }

        }
        return instance;
    }

    boolean end = true;

    public void parseBigLargeData(String uuid, byte[] value) {
        if (uuid_notify.equals(uuid)) {
            if (value.length >= 6 && (value[0] & 0xff) == 0xbc && end) {
                dataLength = ByteUtil.bytesToInt(Arrays.copyOfRange(value, 2, 4));
                if (value.length - 6 >= dataLength) {
                    tempData = Arrays.copyOfRange(value, 0, value.length);
                    parseData(tempData);
                    tempData = new byte[]{};
                    end = true;
                } else {
                    end = false;
                    tempData = Arrays.copyOfRange(value, 0, value.length);
                }
            } else {
                tempData = ByteUtil.concat(tempData, value);
                if (tempData.length - 6 == dataLength) {
                    end = true;
                    parseData(tempData);
                    tempData = new byte[]{};
                } else {
                    end = false;
                }
            }
        }
    }

    public void parseData(byte[] data) {
        int notifyKey = data[1];
        if ((data[0] & 0xff) == 0xbc) {
            BaseResponse response1 = BigDataBeanFactory.createBean(notifyKey);
            if (response1 != null) {
                response1.acceptData(data);
                ILargeDataResponse response;
                if ((data[1]) == 0x73) {
//                    Log.i(TAG,"0x73 Notify->"+String.format("%02X", data[1]) );
                    response = LargeDataHandler.getInstance().getNoClearMap().get(notifyKey);
                } else {
                    response = LargeDataHandler.getInstance().getRespMap().get(notifyKey);
                }
                if (response != null) {
                    response.parseData(notifyKey, response1);
                }
            }
        }
    }
}

