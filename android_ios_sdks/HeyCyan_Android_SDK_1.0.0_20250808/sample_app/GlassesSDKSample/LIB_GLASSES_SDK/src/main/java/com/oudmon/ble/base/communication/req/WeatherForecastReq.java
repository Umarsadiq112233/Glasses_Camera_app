package com.oudmon.ble.base.communication.req;

import com.oudmon.ble.base.communication.Constants;
import com.oudmon.ble.base.communication.utils.DataParseUtils;

/**
 * Created by Jxr35 on 2018/3/28
 */

public class WeatherForecastReq extends MixtureReq {

    private WeatherForecastReq() {
        super(Constants.CMD_SEND_WEATHER_FORECAST);
    }

    public static WeatherForecastReq getWriteInstance(final WeatherForecastBuilder builder) {
        return new WeatherForecastReq() {{
            subData = new byte[10];
            subData[0] = (byte) builder.index;
            System.arraycopy(DataParseUtils.intToByteArray((int) builder.timeStamp), 0, subData, 1, 4);
            subData[5] = (byte) builder.weatherType;
            subData[6] = (byte) builder.minDegree;
            subData[7] = (byte) builder.maxDegree;
            subData[8] = (byte) builder.humidity;
            subData[9] = (byte) (builder.takeUmbrella ? 0x01 : 0x02);
        }};
    }

    public static class WeatherForecastBuilder {
        private int index;
        private long timeStamp;
        private int weatherType;
        private int minDegree;
        private int maxDegree;
        private int humidity;
        private boolean takeUmbrella;

        public WeatherForecastBuilder setIndex(int index) {
            this.index = index;
            return this;
        }

        public WeatherForecastBuilder setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public WeatherForecastBuilder setWeatherType(int weatherType) {
            this.weatherType = weatherType;
            return this;
        }

        public WeatherForecastBuilder setMinDegree(int minDegree) {
            this.minDegree = minDegree;
            return this;
        }

        public WeatherForecastBuilder setMaxDegree(int maxDegree) {
            this.maxDegree = maxDegree;
            return this;
        }

        public WeatherForecastBuilder setHumidity(int humidity) {
            this.humidity = humidity;
            return this;
        }

        public WeatherForecastBuilder setTakeUmbrella(boolean takeUmbrella) {
            this.takeUmbrella = takeUmbrella;
            return this;
        }

        @Override
        public String toString() {
            return "WeatherForecastBuilder{" +
                    "index=" + index +
                    ", timeStamp=" + timeStamp +
                    ", weatherType=" + weatherType +
                    ", minDegree=" + minDegree +
                    ", maxDegree=" + maxDegree +
                    ", humidity=" + humidity +
                    ", takeUmbrella=" + takeUmbrella +
                    '}';
        }
    }

}
