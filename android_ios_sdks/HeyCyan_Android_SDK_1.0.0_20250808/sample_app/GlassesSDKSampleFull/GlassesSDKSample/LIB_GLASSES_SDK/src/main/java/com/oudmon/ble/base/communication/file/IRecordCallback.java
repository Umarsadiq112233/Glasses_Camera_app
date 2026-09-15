package com.oudmon.ble.base.communication.file;
import com.oudmon.ble.base.communication.entity.RecordEntity;
import java.util.ArrayList;

/**
 * Created by Jxr35 on swatch_device_text4/30
 */
public interface IRecordCallback {

    String TAG = "RecordEntity";

    void onFileNames(ArrayList<RecordEntity> fileNames);

    void onProgress(float percent);

    void onComplete();

    void onReceiver(byte[] data);

    void onActionResult( int errCode);
}
