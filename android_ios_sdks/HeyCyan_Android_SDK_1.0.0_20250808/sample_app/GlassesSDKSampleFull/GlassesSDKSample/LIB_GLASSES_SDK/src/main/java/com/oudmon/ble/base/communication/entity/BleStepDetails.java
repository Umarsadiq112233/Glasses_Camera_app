package com.oudmon.ble.base.communication.entity;

/**
 * Created by Jxr35 on 2018/3/2 based on ouweibin
 */
public class BleStepDetails {

    private int year;
    private int month;
    private int day;
    private int timeIndex=0;
    private int calorie=0;
    private int walkSteps=0;
    private int distance=0;
    private int runSteps=0;

    @Override
    public String toString() {
        return "BleStepDetails{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", timeIndex=" + timeIndex +
                ", calorie=" + calorie +
                ", walkSteps=" + walkSteps +
                ", distance=" + distance +
                ", runSteps=" + runSteps +
                '}';
    }

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

    public int getCalorie() {
        return calorie;
    }

    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    public int getWalkSteps() {
        return walkSteps;
    }

    public void setWalkSteps(int walkSteps) {
        this.walkSteps = walkSteps;
    }

    public int getRunSteps() {
        return runSteps;
    }

    public void setRunSteps(int runSteps) {
        this.runSteps = runSteps;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }


}
