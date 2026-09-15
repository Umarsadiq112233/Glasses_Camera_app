package com.oudmon.ble.base.communication.req;


/**
 * Created by Jxr35 on 2018/3/5 based on lehow
 * 读取电量
 * 定时血压数据获取
 * 读取久坐提醒
 * 查询数据存储分布
 * 获取当前运动信息 (实时运动信息)
 * 获取ANCS的开关状态
 * 4.7 读取某天总运动信息
 * 硬重启设备
 */

public class SimpleKeyReq extends BaseReqCmd {


    /**
     * @param key Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE 读取电量
     *            Constants.CMD_BP_TIMING_MONITOR_DATA 定时血压数据获取 BpDataRsp
     *            Constants.CMD_GET_SIT_LONG 读取久坐提醒
     *            Constants.CMD_QUERY_DATA_DISTRIBUTION  查询数据存储分布
     *            Constants.CMD_GET_STEP_TODAY  获取当前运动信息 (实时运动信息)
     *            Constants.CMD_GET_ANCS_ON_OFF  获取ANCS的开关状态
     *            Constants.CMD_GET_STEP_TOTAL_SOMEDAY  读取某天总运动信息  TotalSportDataRsp
     *            Constants.CMD_RE_BOOT 硬重启设备
     */
    public SimpleKeyReq(byte key) {
        super(key);
    }



    @Override
    protected byte[] getSubData() {
        return null;
    }
}
