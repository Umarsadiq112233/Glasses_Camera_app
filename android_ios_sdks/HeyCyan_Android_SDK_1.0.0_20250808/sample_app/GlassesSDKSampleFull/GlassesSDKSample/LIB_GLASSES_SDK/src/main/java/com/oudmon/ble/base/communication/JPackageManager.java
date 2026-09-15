package com.oudmon.ble.base.communication;

/**
 * Created by Jxr35 on swatch_device_text4/7
 */
public class JPackageManager {

    private int mLength = 244;

    private static JPackageManager mInstance;

    private JPackageManager() {

    }

    public static JPackageManager getInstance() {
        if (mInstance == null) {
            synchronized (JPackageManager.class) {
                if (mInstance == null) {
                    mInstance = new JPackageManager();
                }
            }
        }
        return mInstance;
    }

    public void setLength(int length) {
        mLength = length;
    }

    public int getLength() {
        return Math.max(mLength, 244);
    }

}
