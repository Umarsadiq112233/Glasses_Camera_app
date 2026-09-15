package com.oudmon.ble.base.communication.req;
import com.oudmon.ble.base.communication.Constants;
import com.oudmon.qc_utils.bytes.DataTransferUtils;

/**
 * Created by Jxr35 on 2018/6/25
 * 健康目标设置
 */

public class TargetSettingReq extends MixtureReq {

    private TargetSettingReq() {
        super(Constants.CMD_TARGET_SETTING);
    }

    public static TargetSettingReq getReadInstance() {
        return new TargetSettingReq() {{
            subData = new byte[] {0x01};
        }};
    }

    public static TargetSettingReq getWriteInstance(final int step, final int calorie, final int distance) {
        //Log.i(TAG, "Step: " + DataTransferUtils.getHexString(DataTransferUtils.intToBytes(step)));
        return new TargetSettingReq() {{
            subData = new byte[10];
            subData[0] = 0x02;
            System.arraycopy(DataTransferUtils.intToBytes(step), 0, subData, 1, 3);
            System.arraycopy(DataTransferUtils.intToBytes(calorie), 0, subData, 4, 3);
            System.arraycopy(DataTransferUtils.intToBytes(distance), 0, subData, 7, 3);
            //subData = new byte[] {0x02, (byte) step, (byte) calorie, (byte) distance};
        }};
    }

    public static TargetSettingReq getWriteInstance(final int step, final int calorie, final int distance, final int sportMinute, final int sleepMinute) {
        //Log.i(TAG, "Step: " + DataTransferUtils.getHexString(DataTransferUtils.intToBytes(step)));
        return new TargetSettingReq() {{
            subData = new byte[14];
            subData[0] = 0x02;
            System.arraycopy(DataTransferUtils.intToBytes(step), 0, subData, 1, 3);
            System.arraycopy(DataTransferUtils.intToBytes(calorie), 0, subData, 4, 3);
            System.arraycopy(DataTransferUtils.intToBytes(distance), 0, subData, 7, 3);
            System.arraycopy(DataTransferUtils.shortToBytes((short) sportMinute), 0, subData, 10, 2);
            System.arraycopy(DataTransferUtils.shortToBytes((short) sleepMinute), 0, subData, 12, 2);
        }};
    }


}
