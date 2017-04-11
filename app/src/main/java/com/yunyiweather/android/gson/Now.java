package com.yunyiweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 使用GSON方式解析JSON数据,定义实体类"now"
 * Created by 张云天 on 2017/4/11.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("tex")
        public String info;
    }
}
