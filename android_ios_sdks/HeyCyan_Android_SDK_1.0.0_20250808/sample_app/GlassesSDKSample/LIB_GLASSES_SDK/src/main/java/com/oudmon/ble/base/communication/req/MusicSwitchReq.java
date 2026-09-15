package com.oudmon.ble.base.communication.req;



import com.oudmon.ble.base.communication.Constants;

import java.nio.charset.StandardCharsets;

/**
 * Created by Jxr35 on 2018/5/10
 */

public class MusicSwitchReq extends BaseReqCmd {

    protected byte[] data;

    private MusicSwitchReq() {
        super(Constants.CMD_GET_MUSIC_SWITCH);
    }

    /**
     * 读取音乐开关操作
     * @return instance
     */
    public static MusicSwitchReq getReadInstance() {
        return new MusicSwitchReq() {{
            data = new byte[] {0x01};
        }};
    }

    /**
     * 写入音乐开关操作
     * @param enable 开或者关
     * @return instance
     */
    public static MusicSwitchReq getWriteInstance(final boolean enable) {
        return new MusicSwitchReq() {{
            data = new byte[] {0x02, (byte) (enable ? 0x01 : 0x02)};
        }};
    }


    /**
     * 写入音乐开关操作
     * @return instance
     */
    public static MusicSwitchReq getNewWriteInstance(final boolean playing, final int progress, final int volume, final String name) {
        return new MusicSwitchReq() {{
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            byte[] data = new byte[nameBytes.length + 3];
            data[0] = (byte) (playing ? 0 : 1);
            data[1] = (byte) progress;
            data[2] = (byte) volume;
            System.arraycopy(nameBytes, 0, data, 3, nameBytes.length);      //data[1,2,3,4]为数据长度
            this.data = data;
        }};
    }

    @Override
    protected byte[] getSubData() {
        return data;
    }

}
