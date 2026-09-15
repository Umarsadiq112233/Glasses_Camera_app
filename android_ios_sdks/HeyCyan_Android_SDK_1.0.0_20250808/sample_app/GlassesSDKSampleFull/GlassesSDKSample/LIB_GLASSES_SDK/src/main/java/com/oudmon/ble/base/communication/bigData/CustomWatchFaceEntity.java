package com.oudmon.ble.base.communication.bigData;
import java.util.List;
/**
 * @author gs ,
 * @date /5/26
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class CustomWatchFaceEntity {
    List<CustomElement> data;

    public List<CustomElement> getData() {
        return data;
    }

    public void setData(List<CustomElement> data) {
        this.data = data;
    }

    public static class CustomElement{
        int type;
        int x;
        int y;
        int r;
        int g;
        int b;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getR() {
            return r;
        }

        public void setR(int r) {
            this.r = r;
        }

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }
}
