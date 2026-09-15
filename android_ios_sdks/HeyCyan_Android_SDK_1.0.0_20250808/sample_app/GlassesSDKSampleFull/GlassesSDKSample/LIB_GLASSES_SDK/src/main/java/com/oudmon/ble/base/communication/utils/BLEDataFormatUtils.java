package com.oudmon.ble.base.communication.utils;

/**
 * 通信协议数据格式转换
 * Created by Jxr35 on 2018/3/5 based on lehow
 */
public class BLEDataFormatUtils {

    /**
     * 将BCD码转成十进制数
     *
     * @param data
     * @return
     */
    public static int BCDToDecimal(byte data) {
        int decade = (data >> 4) & 0x0F;
        int unit = data & 0x0F;
        return decade * 10 + unit;
    }

    /**
     * 将十进制数转成BCD码，
     * 比如十进制数23的十六进制为17，需要转成BCD码为23存储
     *
     * @param data
     * @return
     */
    public static byte decimalToBCD(int data) {
        int unit = data % 10;
        int decade = data / 10;
        return (byte) ((decade << 4) | unit);
    }

    /**
     * 将字节数组转换成int型，数组的高字节在前
     *
     * @param data
     * @return
     */
    public static int bytes2Int(byte[] data) {
        int length = data.length;
        int res = 0;
        for (int i = 0; i < length; i++) {
            res |= (data[i] & 0xFF) << (8 * (length - 1 - i));
        }
        return res;
    }
}
