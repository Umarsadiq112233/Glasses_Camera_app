package com.oudmon.ble.base.communication.req;


import com.oudmon.ble.base.communication.Constants;

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

public class SimpleKeyPowerOffReq extends MixtureReq {


    public SimpleKeyPowerOffReq() {
        super(Constants.CMD_RE_BOOT);
        subData = new byte[] {0x01};
    }
}
