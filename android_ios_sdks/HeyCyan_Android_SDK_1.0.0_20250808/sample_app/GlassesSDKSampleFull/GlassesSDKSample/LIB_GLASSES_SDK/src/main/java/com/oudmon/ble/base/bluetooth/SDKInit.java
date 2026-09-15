package com.oudmon.ble.base.bluetooth;

public class SDKInit {
    private static SDKInit sdkInit = null;
    public static int SDK_TYPE_QC=1;
    public static int SDK_TYPE_MY=2;
    private int currSDK=1;

    public static SDKInit getInstance() {
        if (sdkInit == null) {
            synchronized (SDKInit.class) {
                if (sdkInit == null) {
                    sdkInit = new SDKInit();
                }
            }
        }
        return sdkInit;
    }


    public int getSdkType(){
        return currSDK;
    }

    public void setSDKType(int type){
       this.currSDK=type;
    }
}
