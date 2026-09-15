package com.oudmon.ble.base.communication.responseImpl;
import static com.oudmon.ble.base.bluetooth.QCDataParser.TAG;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;


import com.oudmon.ble.base.communication.CommandHandle;
import com.oudmon.ble.base.communication.ICommandResponse;
import com.oudmon.ble.base.communication.req.MusicSwitchReq;
import com.oudmon.ble.base.communication.rsp.BaseRspCmd;
import com.oudmon.ble.base.communication.rsp.MusicCommandRsp;

/**
 * @author gs ,
 * @date /1/21
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class MusicCommandListener implements ICommandResponse<MusicCommandRsp> {

    private Context mContext;

    public MusicCommandListener(Context context) {
        mContext = context;
    }

    @Override
    public void onDataResponse(final MusicCommandRsp resultEntity) {
        if (resultEntity.getStatus() == BaseRspCmd.RESULT_OK) {
            if(resultEntity.getAction()==1){
                KeyEvent down = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                KeyEvent down2 = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK);
                KeyEvent up = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                controlMusic(mContext,down,up,down2);
            }else if(resultEntity.getAction()==2){
                KeyEvent down = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                KeyEvent up = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                controlMusic(mContext,down,up);
            }else if(resultEntity.getAction()==3){
                KeyEvent down = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
                KeyEvent up = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT);
                controlMusic(mContext,down,up);
            }else if(resultEntity.getAction()==4){
                AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
            }else if(resultEntity.getAction()==5){
                AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
            }

        }
    }


    /**
     * 控制音乐
     * @param context
     * @param down
     * @param up
     */
    private void controlMusic(Context context, KeyEvent down, KeyEvent up){
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.dispatchMediaKeyEvent(down);
        audioManager.dispatchMediaKeyEvent(up);
        boolean flag=audioManager.isMusicActive();
        if(flag){
            CommandHandle.getInstance().executeReqCmd(MusicSwitchReq.getNewWriteInstance(false, 0, getSystemVolume(mContext), ""),null);
        }
    }

    /**
     * 控制音乐
     * @param context
     * @param down
     * @param up
     */
    private void controlMusic(Context context, KeyEvent down, KeyEvent up,KeyEvent down1){
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.dispatchMediaKeyEvent(down);
        audioManager.dispatchMediaKeyEvent(up);
        audioManager.dispatchMediaKeyEvent(down1);
        boolean flag=audioManager.isMusicActive();
        if(flag){
            CommandHandle.getInstance().executeReqCmd(MusicSwitchReq.getNewWriteInstance(false, 0, getSystemVolume(mContext), ""),null);
        }
    }


    private int getSystemVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return current * 100 / max;
    }
}



