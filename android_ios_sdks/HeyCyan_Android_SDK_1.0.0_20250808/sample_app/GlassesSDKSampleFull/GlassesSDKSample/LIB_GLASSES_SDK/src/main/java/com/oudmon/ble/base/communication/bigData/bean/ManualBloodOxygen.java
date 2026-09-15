package com.oudmon.ble.base.communication.bigData.bean;
import java.util.List;

public class ManualBloodOxygen {
    private int index;
    private List<DetailBean> data;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<DetailBean> getData() {
        return data;
    }

    public void setData(List<DetailBean> data) {
        this.data = data;
    }

    public static class DetailBean{
        /**时间**/
        private int m;
        /**值**/
        private int v;

        public int getM() {
            return m;
        }

        public void setM(int m) {
            this.m = m;
        }

        public int getV() {
            return v;
        }

        public void setV(int v) {
            this.v = v;
        }
    }
}
