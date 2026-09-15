package com.oudmon.ble.base.communication.file;
import java.util.ArrayList;

/**
 * Created by Jxr35 on swatch_device_text4/30
 */
public interface IEbookCallback {

    String TAG = "IEbookCallback";

    void onFileNames(ArrayList<String> fileNames);

    void onProgress(float percent);

    void onComplete();

    void onDeleteSuccess(int code);

    void onActionResult( int errCode);
}
