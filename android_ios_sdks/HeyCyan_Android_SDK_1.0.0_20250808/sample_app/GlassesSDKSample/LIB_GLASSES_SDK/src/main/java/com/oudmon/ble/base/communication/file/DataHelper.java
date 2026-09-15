package com.oudmon.ble.base.communication.file;
import android.util.Log;

import com.oudmon.ble.base.communication.dfu_temperature.TemperatureEntity;
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureOnceEntity;
import com.oudmon.qc_utils.bytes.DataTransferUtils;
import com.oudmon.qc_utils.date.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Jxr35 on swatch_device_text4/29
 */
public class DataHelper {

    private static final String TAG = "DataHelper";
    private static DataHelper mInstance;
    public static DataHelper getInstance() {
        if (mInstance == null) {
            synchronized (DataHelper.class) {
                if (mInstance == null) {
                    mInstance = new DataHelper();
                }
            }
        }
        return mInstance;
    }

    static List<PlateEntity> parsePlate(byte[] data) {
        Log.i(TAG, "=========================== Parse Plate Start ============================");
        List<PlateEntity> mPlateArray = new ArrayList<>();
        int length = data[0];
        Log.i(TAG, "length: " + length);
        try {
            //TODO 解析协议
            int index = 1;
            while (index < data.length) {
                final int delete = data[index++] & 0xff;
                int nameLength = data[index++] & 0xff;
                byte[] byteName = new byte[nameLength];
                System.arraycopy(data, index, byteName, 0, nameLength);
                index += nameLength;
                final String name = new String(byteName);
//               Log.i(TAG,.e( "delete: " + delete + ", nameLength: " + nameLength + ", name: " + name);
                mPlateArray.add(new PlateEntity((delete == 1),name));
            }
            Log.i(TAG, "=========================== Parse Plate End ============================"+mPlateArray.size());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "=========================== Parse Plate Error ============================");
        }
        return mPlateArray;
    }

    static TemperatureEntity parseTemperature(byte[] data) {
        Log.i(TAG, "=========================== ParseTemperature Start ============================");
        TemperatureEntity temperature = new TemperatureEntity();
        try {
            temperature.mIndex = data[0];
            temperature.mTimeSpan = data[1];
            temperature.mValues = new float[data[1] == 0 ? 1 : 60 * 24 / temperature.mTimeSpan];
            //TODO 解析协议
            int index = 2;
            int i = 0;
            while (index < data.length) {
//                Log.i(TAG,index);
//                int value = data[index] & 0xff;
//                if (value > 0x80) {
//                    try {
//                        int length = value - 0x80;
//                        int temp = 0;
//                        //历史遗留看不懂
//                        while (temp < length) {
//                            temperature.mValues[i++] = 0;
//                            temp ++;
//                        }
//                    } catch (Exception e) {
//                       e.printStackTrace();
//                    }
//                } else {
//                    int temp = data[index];
//                    float val = temp * 1F / 10;
//                    temperature.mValues[i++] = val + 32;
//                }
                int temp = data[index]& 0xff;
                float val = temp * 1F / 10;
                temperature.mValues[i++] = val + 20;
                index++;
            }
            Log.i(TAG, "temperature: " + temperature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "=========================== ParseTemperature End ============================");
        return temperature;
    }

    static List<TemperatureOnceEntity> parseTemperatureOnce(byte[] data) {
        Log.i(TAG, "=========================== ParseTemperatureOnce Start ============================");
        List<TemperatureOnceEntity> mArrays = new ArrayList<>();
        try {
            //data[0]
            DateUtil dateUtil=new DateUtil();
            dateUtil.addDay(-data[0]);

            //TODO 解析协议
            int index = 1;
            while (index < data.length) {
                TemperatureOnceEntity temperature = new TemperatureOnceEntity();
                temperature.mTime = dateUtil.getZeroTime() + DataTransferUtils.bytesToShort(data, index) * 60;
                index += 2;

                int temp = data[index++];
                float val = temp * 1F / 10;
                temperature.mValue = val + 20;
                mArrays.add(temperature);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
         Log.i(TAG, "=========================== ParseTemperatureOnce End ============================");
//         Log.i(TAG, mArrays);
        return mArrays;
    }

}
