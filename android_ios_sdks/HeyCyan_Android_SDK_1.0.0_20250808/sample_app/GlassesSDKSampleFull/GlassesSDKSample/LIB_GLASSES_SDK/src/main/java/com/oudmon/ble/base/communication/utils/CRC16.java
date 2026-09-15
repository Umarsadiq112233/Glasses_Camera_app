
package com.oudmon.ble.base.communication.utils;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */
public class CRC16 {

    public static int calcCrc16(byte[] bufData) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;


        if (bufData.length == 0) {
            return CRC;
        }
        for (i = 0; i < bufData.length; i++) {
            CRC ^= ((int) bufData[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        return CRC & 0xffff;
    }


}

