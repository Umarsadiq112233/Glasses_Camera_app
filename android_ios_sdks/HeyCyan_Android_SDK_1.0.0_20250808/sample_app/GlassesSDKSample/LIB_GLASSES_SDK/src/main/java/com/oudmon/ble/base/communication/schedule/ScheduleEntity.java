package com.oudmon.ble.base.communication.schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jxr35 on 2018/8/8
 */

public class ScheduleEntity {

    /**
     * 提醒内容，手环端限制为90位
     */
    public String mTitle;
    /**
     * 提醒的起始时间，
     */
    public int mStartTime;
    /**
     * 提醒的结束时间，到这个时间即结束提醒
     */
    public int mEndTime;
    /**
     * 提醒类型，<br/>
     * 0->无提醒（不应该发给手环），<br/>
     * 1->单次提醒，<br/>
     * 2->每天提醒，<br/>
     * 3->每周提醒，<br/>
     * 4->每月提醒，<br/>
     * 5->每年提醒。<br/>
     */
    public int mRepeatType;
    /**
     * 提醒的具体细节，按协议来：<br/>
     * 0000 1111 表示周一到周四提醒<br/>
     * 0101 0101 表示周一、周三、周五，周日提醒<br/>
     */
    public int mDetail;

    /**
     * 提醒的具体细节：<br/>
     * 每周提醒时，1表示周一，7表示周日。<br/>
     * 每月提醒时，1表示1号，30表示30号。<br/>
     * 每年提醒时，1表示元旦，300表示一年中的第300天<br/>
     */
    public List<Integer> mDetails = new ArrayList<>();


    public ScheduleEntity() {

    }

    public ScheduleEntity(String mTitle, int mStartTime, int mEndTime, int mRepeatType, int mDetail) {
        this.mTitle = mTitle;
        this.mStartTime = mStartTime;
        this.mEndTime = mEndTime;
        this.mRepeatType = mRepeatType;
        this.mDetail = mDetail;
    }
}
