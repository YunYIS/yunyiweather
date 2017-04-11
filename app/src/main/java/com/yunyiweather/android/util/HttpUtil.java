package com.yunyiweather.android.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * (util包用于存放工具相关的代码)
 * 使用OkHttp与服务器进行交互,调用setOkHttpRequest()方法发起一条HTTP请求(传入请求地址,并注册一个回调来处理服务器响应)
 * Created by 张云天 on 2017/4/10.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);//异步请求
    }
}
