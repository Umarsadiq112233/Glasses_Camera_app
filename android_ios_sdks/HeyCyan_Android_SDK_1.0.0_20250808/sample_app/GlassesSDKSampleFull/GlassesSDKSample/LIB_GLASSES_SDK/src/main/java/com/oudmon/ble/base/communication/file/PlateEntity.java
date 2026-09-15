package com.oudmon.ble.base.communication.file;

/**
 * Created by Jxr35 on swatch_device_text6/13
 */
public class PlateEntity {

    public boolean mDelete = false;

    public String mPlateName = "";

    public PlateEntity(boolean mDelete, String mPlateName) {
        this.mDelete = mDelete;
        this.mPlateName = mPlateName;
    }

    @Override
    public String toString() {
        return "PlateEntity{" +
                "mDelete=" + mDelete +
                ", mPlateName='" + mPlateName + '\'' +
                '}';
    }
}
