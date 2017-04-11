package com.yunyiweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 总实体类,用来引用创建的各个实体类
 * Created by 张云天 on 2017/4/11.
 */

public class Weather {
    public String status;//成功返回ok, 失败会返回具体的原因.
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
