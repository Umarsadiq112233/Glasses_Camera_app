package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.10 设置时间格式
 * 设置12/24小时显示制
 * 公英制也加入到该命令中
 */

public class TimeFormatReq extends MixtureReq {

    private TimeFormatReq() {
        super(Constants.CMD_GET_TIME_SETTING);
        subData = new byte[] {0x01};
    }

    private TimeFormatReq(boolean is24, byte metric) {
        super(Constants.CMD_GET_TIME_SETTING);
        subData = new byte[] {0x02, (byte) (is24 ? 0x00 : 0x01), metric};
    }

    public static TimeFormatReq getReadInstance() {
        return new TimeFormatReq();
    }

    /**
     * 12小时制还是24小时制，公制还是英制
     *
     * @param is24   时制
     * @param metric 公英制
     * @return instance
     */
    public static TimeFormatReq getWriteInstance(boolean is24, byte metric) {
        return new TimeFormatReq(is24, metric);
    }

    /**
     * 写入用户参数
     *
     * @param is24     时制
     * @param metric   公英制
     * @param sex      性别（0 = 男， 1 = 女）
     * @param age      年龄（岁）
     * @param height   身高（厘米）
     * @param weight   体重（Kg）
     * @param sbp      收缩压（mmHg）
     * @param dbp      舒张压（mmHg）
     * @param rateWarn 心率报警值(bpm)
     * @return instance
     */
    public static TimeFormatReq getWriteInstance(final boolean is24, final int metric, final int sex, final int age, final int height, final int weight, final int sbp, final int dbp, final int rateWarn) {
        return new TimeFormatReq() {{
            subData = new byte[] {0x02, (byte) (is24 ? 0x00 : 0x01), (byte) metric, (byte) sex, (byte) age, (byte) height, (byte) weight, (byte) sbp, (byte) dbp, (byte) rateWarn};
        }};
    }

    /**
     * 写入用户参数
     *
     * @param is24     时制
     * @param metric   公英制
     * @param sex      性别（0 = 男， 1 = 女）
     * @param age      年龄（岁）
     * @param height   身高（厘米）
     * @param weight   体重（Kg）
     * @param sbp      收缩压（mmHg）
     * @param dbp      舒张压（mmHg）
     * @param rateWarn 心率报警值(bpm)
     * @return instance
     */
    public static TimeFormatReq getWriteInstance(final boolean is24, final boolean metric, final int sex, final int age, final int height, final int weight, final int sbp, final int dbp, final int rateWarn,int open) {
        return new TimeFormatReq() {{
            subData = new byte[] {0x02, (byte) (is24 ? 0x00 : 0x01), (byte) (metric ? 0x00 : 0x01), (byte) sex, (byte) age, (byte) height, (byte) weight, (byte) sbp, (byte) dbp, (byte) rateWarn, (byte) open};
        }};
    }

}
