package com.oudmon.ble.base.communication.file;
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureEntity;
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureOnceEntity;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Jxr35 on swatch_device_text4/30
 */
public interface ICallback {

    String TAG = "ICallback";

    void onRequestAGPS();

    void onUpdatePlate(List<PlateEntity> array);

    void onUpdatePlateError(int code);

    void onDeletePlate();

    void onDeletePlateError(int code);

    void onUpdateTemperature(TemperatureEntity data);

    void onUpdateTemperatureList(List<TemperatureOnceEntity> array);

    void onFileNames(ArrayList<String> fileNames);

    void onProgress(int percent);

    void onComplete();

    void onActionResult(int type, int errCode);
}
