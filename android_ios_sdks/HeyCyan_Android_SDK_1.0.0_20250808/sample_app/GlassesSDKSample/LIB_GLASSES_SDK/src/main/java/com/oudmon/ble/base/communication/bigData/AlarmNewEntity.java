package com.oudmon.ble.base.communication.bigData;

import java.util.List;

public class AlarmNewEntity {
    private int total;
    private List<AlarmBean> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<AlarmBean> getData() {
        return data;
    }

    public void setData(List<AlarmBean> data) {
        this.data = data;
    }

    public static class AlarmBean{
        private int alarmLength;
        private int repeatAndEnable;
        private int min;
        private String content;

        public int getAlarmLength() {
            return alarmLength;
        }

        public void setAlarmLength(int alarmLength) {
            this.alarmLength = alarmLength;
        }

        public int getRepeatAndEnable() {
            return repeatAndEnable;
        }

        public void setRepeatAndEnable(int repeatAndEnable) {
            this.repeatAndEnable = repeatAndEnable;
        }

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}
