package com.oudmon.ble.base.communication.bigData.bean;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import java.nio.charset.Charset;

public class WifiInfoReq {
    private byte[] mData = new byte[2];

    public WifiInfoReq(String url)  {
        mData[0] = 0x02;
        mData[1] = (byte) url.length();
        byte [] urlBytes = url.getBytes(Charset.forName("UTF-8"));
        mData= ByteUtil.concat(mData,urlBytes);
    }
    public byte[] getSubData() {
        return mData;
    }

}
