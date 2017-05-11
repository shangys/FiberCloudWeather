package com.fibercloudweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 未来的天气情况 包括日期、温度高低、是否有雨
 * Created by kevin on 2017/5/10.
 */

public class Forecast {
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature{
        public String max;
        public String min;
    }
    public class More{
        @SerializedName("txt_d")
        public String info;
    }
}
