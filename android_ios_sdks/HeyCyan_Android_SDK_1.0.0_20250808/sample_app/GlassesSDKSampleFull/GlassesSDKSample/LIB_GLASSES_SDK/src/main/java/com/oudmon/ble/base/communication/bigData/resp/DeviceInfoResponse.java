package com.oudmon.ble.base.communication.bigData.resp;
import android.util.Log;

import com.oudmon.ble.base.communication.utils.ByteUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DeviceInfoResponse extends BaseResponse {
    private byte[] subData;
    private String firmwareVersion="";
    private String hardwareVersion="";
    private String wifiFirmwareVersion="";
    private String wifiHardwareVersion="";

    @Override
    public boolean acceptData(byte[] data) {
        subData=data;
        try {
            //bc4349009164 00 1200 0800 1a00 0c00 4130325f312e30302e30345f3234313232334130325f56312e30574946494130325f302e30372e30315f32353033313031313433574946494130325f56312e30
            int twsFm= ByteUtil.bytesToInt(Arrays.copyOfRange(data, 7, 9));
            int twsHw= ByteUtil.bytesToInt(Arrays.copyOfRange(data, 9, 11));
            int socFm= ByteUtil.bytesToInt(Arrays.copyOfRange(data, 11, 13));
            int socHw= ByteUtil.bytesToInt(Arrays.copyOfRange(data, 13, 15));

//            Log.i(TAG,twsFm+"-"+twsHw+"-"+socFm+"-"+socHw);

            firmwareVersion=(new String(Arrays.copyOfRange(data, 15, 15 + twsFm), StandardCharsets.UTF_8));
            hardwareVersion=(new String(Arrays.copyOfRange(data, 15 + twsFm, 15 + twsFm+twsHw), StandardCharsets.UTF_8));
            wifiFirmwareVersion=(new String(Arrays.copyOfRange(data, 15 + twsFm+twsHw, 15 + twsFm+twsHw+socFm), StandardCharsets.UTF_8));
            wifiHardwareVersion=(new String(Arrays.copyOfRange(data, 15 + twsFm+twsHw+socFm, 15 + twsFm+twsHw+socFm+socHw), StandardCharsets.UTF_8));
            Log.i(TAG,"firmwareVersion="+firmwareVersion+",hardwareVersion="+hardwareVersion+",wifiFirmwareVersion="+wifiFirmwareVersion+",wifiHardwareVersion="+wifiHardwareVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public byte[] getSubData() {
        return subData;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getWifiFirmwareVersion() {
        return wifiFirmwareVersion;
    }

    public String getWifiHardwareVersion() {
        return wifiHardwareVersion;
    }
}
