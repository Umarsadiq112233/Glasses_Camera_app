package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.ByteUtil;
/**
 *设置经纬度
 */

//public class SetLocationReq extends BaseReqCmd {BaseReqCmd
//
//    private  byte[] mData = new byte[11];
//
//    public SetLocationReq(double longitude,double latitude) {
//        super(Constants.CMD_GPS_LOCATION);
//
//        int du1 = (int) latitude;
//        double tp = (latitude - du1) * 60;
//        int fen = (int) tp;
//        float second= (float) Math.abs(((tp - fen) * 60));
//        int value= (int) second;
//
//        int du2 = (int) longitude;
//        double tp1 = (longitude - du2) * 60;
//        int fen1 = (int) tp1;
//        float second1= (float) Math.abs(((tp1 - fen1) * 60));
//        int value1= (int) second1;
//        mData[0]=0x02;
//        mData[1]= (byte) ByteUtil.hiword(du1);
//        mData[2]= (byte) ByteUtil.loword(du1);
//        mData[3]= (byte) Math.abs(fen);
//        mData[4]= (byte) value;
//        mData[5]= (byte) ((second-(value))*10000/100);
//
//        mData[6]= (byte) ByteUtil.hiword(du2);
//        mData[7]= (byte)  ByteUtil.loword(du2);
//        mData[8]= (byte) Math.abs(fen1);
//        mData[9]= (byte) value1;
//        mData[10]= (byte) ((second1-(value1))*10000/100);
//
////         Log.i(TAG,mData);
//    }
//
//    @Override
//    protected byte[] getSubData() {
//        return mData;
//    }

//}
