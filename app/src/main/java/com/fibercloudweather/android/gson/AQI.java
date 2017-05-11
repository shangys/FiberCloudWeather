package com.fibercloudweather.android.gson;

/**
 * 一些天气指标 包括空气质量指数aqi、pm2.5
 * Created by kevin on 2017/5/10.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
