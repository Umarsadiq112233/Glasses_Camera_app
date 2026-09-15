package com.oudmon.ble.base.communication.entity;

import java.util.Arrays;

/**
 * Created by Jxr35 on 2018/3/2 based on ouweibin
 */

public class BleSleepDetails {

    private int year;
    private int month;
    private int day;
    private int timeIndex;
    private int[] sleepQualities;


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getTimeIndex() {
        return timeIndex;
    }

    public void setTimeIndex(int timeIndex) {
        this.timeIndex = timeIndex;
    }

    public int[] getSleepQualities() {
        return sleepQualities;
    }

    public void setSleepQualities(int[] sleepQualities) {
        this.sleepQualities = sleepQualities;
    }

    @Override
    public String toString() {
        return "BleSleepDetails{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", timeIndex=" + timeIndex +
                ", sleepQualities=" + Arrays.toString(sleepQualities) +
                '}';
    }

}
