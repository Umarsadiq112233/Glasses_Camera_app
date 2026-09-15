package com.oudmon.ble.base.bluetooth.spp;

import android.os.Bundle;

 public interface MyLocalPlaybackModelCallback {
     void onGetFileHeaderReport(int var1, long var2);

     void onGetFileContentReport(int var1, byte[] var2);

     void onGetFileFooterReport(int var1, byte[] var2);

     void onAddOrDeleteSongToPlaylistReport(int var1) ;

     void onDeleteSingleSongReport(int var1) ;

     void onDeleteAllSongReport(int var1);

     void onGetDeviceInfoReport(Bundle var1);

     void onEnterSongTransferModeReport(boolean var1);

     void onExitSongTransferModeReport(boolean var1);

     void onCancelTransferReport(boolean var1);

     void onWriteSuccessReport(int var1);

     void onWriteFailedReport();

     void onTransferWasValidReport(int var1);
}
