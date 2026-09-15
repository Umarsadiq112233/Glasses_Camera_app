package com.oudmon.ble.base.communication.rsp;

/**
 * Created by Jxr35 on 2018/3/29
 */

public class BrightnessSettingsRsp extends MixtureRsp {

    private int level = 0;

    /**
     * 这里的subData已经去掉了前面的CMD数据，比如0x1B实现已经未包含在里面。
     *
     * @param subData 去掉了前缀的数据
     */
    @Override
    protected void readSubData(byte[] subData) {
        level = subData[1];
    }

    public int getLevel() {
        return level;
    }

}
