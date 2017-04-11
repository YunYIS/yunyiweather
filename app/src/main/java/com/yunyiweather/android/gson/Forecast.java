package com.yunyiweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 使用GSON方式解析JSON数据
 * 由于daily_forecast中包含的是一个数组,数字中的每一项都代表了未来一天的天气信息,所以对于这种情况,我们只定义单日天气的实体类
 * ,然后再声明实体类引用的时候使用集合类型来进行声明.
 * Created by 张云天 on 2017/4/11.
 */

public class Forecast {
    public String date;
    @SerializedName("tmp")
    public Temperature temerature;
    @SerializedName("cond")
    public More more;
    public class Temperature {
        public String max;
        public String min;
    }
    public class More{
        @SerializedName("txt_d")
        public String info;
    }
}
