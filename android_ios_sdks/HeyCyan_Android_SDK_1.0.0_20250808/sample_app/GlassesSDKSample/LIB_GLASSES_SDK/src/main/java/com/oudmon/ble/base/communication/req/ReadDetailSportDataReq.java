package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;

/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 4.22 取得指定日期详细运动信息： (主要是从flash中读取数据)
 */

public class ReadDetailSportDataReq extends BaseReqCmd {

    private byte[] data;

    /**
     * @param dayOffset  表示具体哪一天的数据，0x00表示当天，0x01表示一天前的数据，0x02表示两天前的数据，依次类推，.....0x1D表示第二十九天前的数据，总共支持三十天；
     * @param startIndex 表示需要获取数据段的开始时间索引, 如0:00 => 0(粒度为BB, 目前为15Min), 8:15 => 33(粒度为BB, 目前为15Min);
     * @param endIndex   表示需要获取数据段的结束时间索引, 如0:00 => 0(粒度为BB, 目前为15Min), 8:15 => 33(粒度为BB, 目前为15Min);
     */
    public ReadDetailSportDataReq(int dayOffset, int startIndex, int endIndex) {
        super(Constants.CMD_GET_STEP_SOMEDAY_DETAIL);
        if (dayOffset > 29) throw new IllegalArgumentException("dayOffset 最大只到29");
        if (startIndex > endIndex || endIndex > 95) throw new IllegalArgumentException("数据段索引值异常");
        data = new byte[] {(byte) dayOffset, 0x0f, (byte) startIndex, (byte) endIndex,0x01};
    }

    @Override
    protected byte[] getSubData() {
        return data;
    }
}
