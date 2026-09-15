package com.oudmon.ble.base.communication.entity;

/**
 * Created by Jxr35 on 2018/3/2 based on ouweibin
 * 某天总运动信息
 */
public class BleStepTotal {

    private int daysAgo; // 第几天前
    private int year;    // 日期：年
    private int month;  // 日期：月
    private int day;    // 日期：日
    private int totalSteps;  // 总步数
    private int runningSteps; // 跑步步数/有氧步数
    private int calorie;    // 卡路里值
    private int walkDistance;  // 步行距离
    private int sportDuration;   // 运动时间，单位为秒
    private int sleepDuration;   // 睡眠时间，单位为秒

    @Override
    public String toString() {
        return "BleStepTotal{" +
                " daysAgo=" + daysAgo +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", totalSteps=" + totalSteps +
                ", runningSteps=" + runningSteps +
                ", calorie=" + calorie +
                ", walkDistance=" + walkDistance +
                ", sportDuration=" + sportDuration +
                ", sleepDuration=" + sleepDuration +
                " }";
    }

    public BleStepTotal clone() {
        BleStepTotal bleStepTotal = new BleStepTotal();
        bleStepTotal.setDaysAgo(daysAgo);
        bleStepTotal.setYear(year);
        bleStepTotal.setMonth(month);
        bleStepTotal.setDay(day);
        bleStepTotal.setTotalSteps(totalSteps);
        bleStepTotal.setRunningSteps(runningSteps);
        bleStepTotal.setCalorie(calorie);
        bleStepTotal.setSportDuration(sportDuration);
        bleStepTotal.setSleepDuration(sleepDuration);
        return bleStepTotal;
    }


    public int getDaysAgo() {
        return daysAgo;
    }

    public void setDaysAgo(int daysAgo) {
        this.daysAgo = daysAgo;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public int getRunningSteps() {
        return runningSteps;
    }

    public void setRunningSteps(int runningSteps) {
        this.runningSteps = runningSteps;
    }

    public int getCalorie() {
        return calorie;
    }

    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    public int getWalkDistance() {
        return walkDistance;
    }

    public void setWalkDistance(int walkDistance) {
        this.walkDistance = walkDistance;
    }

    public int getSportDuration() {
        return sportDuration;
    }

    public void setSportDuration(int sportDuration) {
        this.sportDuration = sportDuration;
    }

    public int getSleepDuration() {
        return sleepDuration;
    }

    public void setSleepDuration(int sleepDuration) {
        this.sleepDuration = sleepDuration;
    }
}
