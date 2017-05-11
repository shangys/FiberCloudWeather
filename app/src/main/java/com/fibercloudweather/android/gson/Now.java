package com.fibercloudweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 现在的天气情况 包括温度高低、是否有雨
 * Created by kevin on 2017/5/10.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;//温度

    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
