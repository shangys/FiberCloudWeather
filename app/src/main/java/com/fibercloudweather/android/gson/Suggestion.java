package com.fibercloudweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 一些建议 包括天气感觉、对使用车的影响、运动
 * Created by kevin on 2017/5/10.
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }
    public class CarWash{
        @SerializedName("txt")
        public String info;
    }
    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
