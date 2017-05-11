package com.fibercloudweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 一些基本情况 包括城市名、城市所对应的天气id、天气更新时间
 * 由于JSON中的一些字段可能不太适合直接作为Java字段来命名，因此这里使用了@SerializedName注解的方式来让JSON字段和Java字段之间建立映射关系
 * Created by kevin on 2017/5/10.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;//城市名

    @SerializedName("id")
    public String weatherId;//城市对应的天气id

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;//天气的更新时间
    }
}
