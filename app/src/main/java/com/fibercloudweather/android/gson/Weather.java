package com.fibercloudweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 总的实例类来引用创建好的各个实体类
 * Created by kevin on 2017/5/10.
 */

public class Weather {
    public String status;//返回的天气数据中还会包含一项status数据，成功返回ok，失败则会返回具体的原因，因此这里也需要添加一个对应的status字段
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;//由于daily_forecast中包含的是一个数组 这里使用List集合来引用Forecast类
}
