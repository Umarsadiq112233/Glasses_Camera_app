package com.oudmon.ble.base.communication.file;

import android.util.Log;


import com.oudmon.ble.base.communication.dfu_temperature.TemperatureEntity;
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureOnceEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jxr35 on swatch_device_text4/30
 */
public class SimpleCallback implements ICallback {

    @Override
    public void onRequestAGPS() {
        Log.i(TAG, "onRequestAGPS..");
    }

    @Override
    public void onUpdatePlate(List<PlateEntity> array) {
    }

    @Override
    public void onUpdatePlateError(int code) {

    }

    @Override
    public void onDeletePlate() {

    }

    @Override
    public void onDeletePlateError(int code) {

    }

    @Override
    public void onUpdateTemperature(TemperatureEntity data) {
        Log.i(TAG, "onUpdateTemperature..");
    }

    @Override
    public void onUpdateTemperatureList(List<TemperatureOnceEntity> array) {
        Log.i(TAG, "onUpdateTemperatureList..");
    }

    @Override
    public void onFileNames(ArrayList<String> fileNames) {


    }

    @Override
    public void onProgress(int percent) {
        Log.i(TAG, "onProgress..");
    }

    @Override
    public void onComplete() {
        Log.i(TAG, "onComplete..");
    }


    @Override
    public void onActionResult(int type, int errCode) {
        Log.i(TAG, "onActionResult..");
    }

}
