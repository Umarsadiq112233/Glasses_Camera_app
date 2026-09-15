package com.oudmon.ble.base.communication.rsp;
import com.oudmon.ble.base.communication.utils.ByteUtil;
import java.util.Arrays;

public class DeviceAvatarRsp extends BaseRspCmd {
    private int screenType;
    private int avatarWidth;
    private int avatarHeight;

    @Override
    public boolean acceptData(byte[] data) {
        screenType = data[0];
        avatarWidth = (ByteUtil.bytesToInt(Arrays.copyOfRange(data, 1, 3)));
        avatarHeight = (ByteUtil.bytesToInt(Arrays.copyOfRange(data, 3, 5)));
        return false;
    }

    public int getScreenType() {
        return screenType;
    }

    public int getAvatarWidth() {
        return avatarWidth;
    }

    public int getAvatarHeight() {
        return avatarHeight;
    }
}
