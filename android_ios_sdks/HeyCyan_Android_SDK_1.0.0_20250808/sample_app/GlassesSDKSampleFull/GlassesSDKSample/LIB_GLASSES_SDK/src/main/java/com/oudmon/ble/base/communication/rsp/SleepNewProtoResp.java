package com.oudmon.ble.base.communication.rsp;

import java.util.List;

/**
 * @author gs
 * @CreateDate: /11/1 17:18
 * <p>
 * "佛主保佑,
 * 永无bug"
 */
public class SleepNewProtoResp {
    private int st;
    private int et;
    private List<DetailBean> list;

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int getEt() {
        return et;
    }

    public void setEt(int et) {
        this.et = et;
    }

    public List<DetailBean> getList() {
        return list;
    }

    public void setList(List<DetailBean> list) {
        this.list = list;
    }

    public static class DetailBean{
        private int d;
        private int t;

        public int getD() {
            return d;
        }

        public void setD(int d) {
            this.d = d;
        }

        public int getT() {
            return t;
        }

        public void setT(int t) {
            this.t = t;
        }
    }
}
