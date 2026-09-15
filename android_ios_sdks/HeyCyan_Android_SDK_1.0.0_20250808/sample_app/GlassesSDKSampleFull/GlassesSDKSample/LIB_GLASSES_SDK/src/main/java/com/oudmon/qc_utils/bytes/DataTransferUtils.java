package com.oudmon.qc_utils.bytes;

public class DataTransferUtils {

    public static String getHexString(byte[] data) {
        StringBuilder s = new StringBuilder();
        if (data != null) {
            for (byte aData : data) {
                String hex = Integer.toHexString(aData & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                s.append(hex);
            }
        }
        return s.toString();
    }


    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }


    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        return ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
    }

    /**
     * 2位的byte数组取int值，低位在前，高位在后
     *
     * @param src    byte数组
     * @param offset 从offset开始
     * @return int数据
     */
    public static int byte2Int(byte[] src, int offset) {
        return (src[offset] & 0xff) | ((src[offset + 1] & 0xff) << 8);
    }

    public static short bytesToShort(byte[] src, int offset) {
        return (short) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8));
    }

    public static byte[] shortToBytes(short value) {
        byte[] src = new byte[2];
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    public static int arrays2Int(byte[] data) {
        if (data.length == 1) {
            return 0xff & data[0];
        } else if (data.length == 2) {
            return bytesToInt(data);
        } else if (data.length == 4) {
            return bytesToInt(data, 0);
        } else {
            return -1;
        }
    }

    /**
     * byte to int
     * @param bytes
     * @return
     */
    public static int bytesToInt(byte[] bytes) {
        int addr = 0;
        if (bytes.length == 1) {
            addr = bytes[0] & 0xFF;
        } else if (bytes.length == 4) {
            addr = bytes[0] & 0xFF;
            addr |= ((bytes[1] << 8) & 0xFF00);
            addr |= ((bytes[2] << 16) & 0xFF0000);
            addr |= ((bytes[3] << 24) & 0xFF000000);
        } else if (bytes.length == 2) {
            addr = bytes[0] & 0xFF;
            addr |= ((bytes[1] << 8) & 0xFF00);
        } else if (bytes.length == 3) {
            addr = bytes[0] & 0xFF;
            addr |= ((bytes[1] << 8) & 0xFF00);
            addr |= ((bytes[2] << 16) & 0xFF0000);
        }
        return addr;
    }

    public static float bytes2Float(byte[] data, int offset) {
        return Float.intBitsToFloat(bytesToInt(data, offset));
    }

    public static int enableWeek(int week){
        return (week & 0x80);
    }

}
