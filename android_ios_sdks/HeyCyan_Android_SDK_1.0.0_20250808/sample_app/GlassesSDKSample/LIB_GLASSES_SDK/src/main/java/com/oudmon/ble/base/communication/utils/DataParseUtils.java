package com.oudmon.ble.base.communication.utils;

/**
 * Created by Jxr35 on 2018/3/5 based on roy
 */

public class DataParseUtils {
    /**
     * 将32位的int值放到4字节的byte数组
     *
     * @param num
     * @return
     */
    public static byte[] intToByteArray(int num) {
        int temp = num;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(temp & 0xff).byteValue();
            temp = temp >> 8;
        }
        return b;
    }

    /**
     * 将4字节的byte数组转成一个int值
     *
     * @param b
     * @return
     */
    public static int byteArrayToInt(byte[] b) {
        int s = 0;
        int s0 = b[0] & 0xff;
        int s1 = b[1] & 0xff;
        int s2 = b[2] & 0xff;
        int s3 = b[3] & 0xff;
        s3 <<= 24;
        s2 <<= 16;
        s1 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }
}
