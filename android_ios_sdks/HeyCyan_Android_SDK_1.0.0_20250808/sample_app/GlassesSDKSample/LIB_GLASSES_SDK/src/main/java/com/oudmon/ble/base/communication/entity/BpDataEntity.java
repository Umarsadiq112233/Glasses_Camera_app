package com.oudmon.ble.base.communication.entity;

import java.util.ArrayList;

/**
 * Created by Jxr35 on 2018/3/2 based on lehow
 */

public class BpDataEntity {

    private int year;
    private int mouth;
    private int day;
    private int timeDelay;
    private ArrayList<BpValue> bpValues;

    public BpDataEntity(int year, int mouth, int day, int timeDelay) {
        this.year = year;
        this.mouth = mouth;
        this.day = day;
        this.timeDelay = timeDelay;
        bpValues = new ArrayList<>();
    }

    public void addBpIndex(int timeMinute) {
        bpValues.add(new BpValue(timeMinute));
    }

    public void addRealValue(int offset, byte[] bytes) {
        int remain = Math.min(bpValues.size() - offset, bytes.length);
        for (int i = 0; i < remain; i++) {
            bpValues.get(i + offset).value = bytes[i] & 0xff;
        }
    }

    public class BpValue {
        int timeMinute;
        int value;

        public BpValue(int timeMinute) {
            this.timeMinute = timeMinute;
        }

        public int getTimeMinute() {
            return timeMinute;
        }

        public int getValue() {
            return value;
        }
    }

    public int getYear() {
        return year;
    }

    public int getMouth() {
        return mouth;
    }

    public int getDay() {
        return day;
    }

    public int getTimeDelay() {
        return timeDelay;
    }

    public ArrayList<BpValue> getBpValues() {
        return bpValues;
    }
}
