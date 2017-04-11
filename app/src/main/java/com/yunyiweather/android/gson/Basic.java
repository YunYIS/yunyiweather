package com.yunyiweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 使用GSON方式解析JSON数据,定义实体类"basic"
 * Created by 张云天 on 2017/4/11.
 */

public class Basic {
    /**
     * 由于JSON中的某些字段可能不太适合直接作为java字段来命名,所以这里使用了@SerializedName注解的方式让JSON字段
     * 和java字段之间建立映射关系
     */
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
