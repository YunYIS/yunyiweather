package com.yunyiweather.android.gson;

/**
 * 使用GSON方式解析JSON数据,定义实体类"aqi"
 * Created by 张云天 on 2017/4/11.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;//空气质量指数
        public String pm25;
    }
}
