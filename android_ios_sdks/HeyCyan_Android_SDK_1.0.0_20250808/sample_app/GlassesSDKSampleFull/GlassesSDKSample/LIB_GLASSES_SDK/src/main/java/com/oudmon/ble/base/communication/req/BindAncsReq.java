package com.oudmon.ble.base.communication.req;
import android.os.Build;
import android.util.Log;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 */

public class BindAncsReq extends BaseReqCmd {

    private byte[] mData;

    public BindAncsReq() {
        super(Constants.CMD_SET_PHONE_OS);
        try {
             Log.i(TAG, "手机厂商: " + Build.BRAND+ "手机型号: " + Build.MODEL+"SDK版本: " + Build.VERSION.SDK_INT+"系统版本: " + Build.VERSION.RELEASE);
            byte[] model = Build.MODEL.getBytes(StandardCharsets.UTF_8);
            if(model.length>=14){
                model= Arrays.copyOf(model,13);
            }
            byte androidVersion = 10;
            if(Build.VERSION.SDK_INT==29){
                androidVersion=10;
            }else if(Build.VERSION.SDK_INT==28){
                androidVersion=9;
            }else if(Build.VERSION.SDK_INT==27 ||Build.VERSION.SDK_INT==26){
                androidVersion=8;
            }else if(Build.VERSION.SDK_INT==25 ||Build.VERSION.SDK_INT==24){
                androidVersion=7;
            }else if(Build.VERSION.SDK_INT==23){
                androidVersion=6;
            }else if(Build.VERSION.SDK_INT==22 ||Build.VERSION.SDK_INT==21 ){
                androidVersion=5;
            }
            mData = new byte[model.length+ 2];
            mData[0] = 0x02;
            mData[1] = androidVersion;
            System.arraycopy(model, 0, mData, 2, model.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected byte[] getSubData() {
        //return new byte[] {0x02};
        return mData == null ? new byte[] {0x02} : mData;
    }
}
