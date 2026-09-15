package com.oudmon.ble.base.communication.entity;

/**
 * Created by Jxr35 on 2018/3/2 based on roy
 */

public class BlePressure {

    public long time;
    public int dbp;
    public int sbp;

    public BlePressure(long time, int sbp, int dbp) {
        this.time = time;
        this.sbp = sbp;
        this.dbp = dbp;
    }
}
