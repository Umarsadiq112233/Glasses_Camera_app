package com.oudmon.ble.base.communication.bigData.resp;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClassBluetoothResponse extends BaseResponse{
    private byte[] subData;
    private String btAddress;
    private String btName;

    @Override
    public boolean acceptData(byte[] data) {
        subData=data;
        btAddress=bytesToMac(Arrays.copyOfRange(data, 6, 12));
        btName=(new String(Arrays.copyOfRange(data, 13, 13 + data[12]), StandardCharsets.UTF_8));

        return false;
    }

    public byte[] getSubData() {
        return subData;
    }

    public String getBtAddress() {
        return btAddress;
    }

    public String getBtName() {
        return btName;
    }

    /**
     * 字节数组转mac地址
     *
     * @param copyOfRange
     * @return
     */
    public String bytesToMac(byte[] copyOfRange) {
        StringBuilder sb = new StringBuilder();
        for (byte b : copyOfRange) {
            String i = Integer.toHexString(b & 0xff).toUpperCase();
            i = i.length() == 1 ? "0" + i : i;
            sb.append(i).append(":");
        }
        return sb.substring(0, sb.length() - 1);
    }

}
