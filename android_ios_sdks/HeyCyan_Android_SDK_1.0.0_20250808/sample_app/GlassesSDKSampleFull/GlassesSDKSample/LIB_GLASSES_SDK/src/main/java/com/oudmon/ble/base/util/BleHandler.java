package com.oudmon.ble.base.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 *
 * Created by hzy on 20200823
 */

public class BleHandler extends Handler {
    private static final String TAG = "BleHandler";
    private static BleHandler sHandler;

    public static BleHandler of(){
        synchronized (BleHandler.class){
            if(sHandler == null){
                HandlerThread handlerThread = new HandlerThread("handler thread");
                handlerThread.start();
                sHandler = new BleHandler(handlerThread.getLooper());
            }
            return sHandler;
        }
    }

    private BleHandler(Looper looper){
        super(Looper.myLooper());
    }
}
