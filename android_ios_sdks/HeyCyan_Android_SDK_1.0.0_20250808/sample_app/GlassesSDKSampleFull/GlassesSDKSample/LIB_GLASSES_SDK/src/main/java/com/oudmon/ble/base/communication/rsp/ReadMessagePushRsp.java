package com.oudmon.ble.base.communication.rsp;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import com.oudmon.qc_utils.bytes.DataTransferUtils;


public class ReadMessagePushRsp extends BaseRspCmd {
   private int deviceSupport1=0;
   private int deviceSupport2=0;
   private int deviceSupport3=0;


    @Override
    public boolean acceptData(byte[] data) {
        //ff9f000000000000000000000000
//         Log.i(TAG, ByteUtil.byteArrayToString(data));
        deviceSupport1= DataTransferUtils.bytesToInt(data, 2);
        deviceSupport2= DataTransferUtils.bytesToInt(data, 4);
        deviceSupport3= DataTransferUtils.bytesToInt(data, 6);
        return false;
    }

    public int getDeviceSupport1() {
        return deviceSupport1;
    }

    public int getDeviceSupport2() {
        return deviceSupport2;
    }

    public int getDeviceSupport3() {
        return deviceSupport3;
    }

}
