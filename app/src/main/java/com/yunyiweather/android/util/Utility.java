package com.yunyiweather.android.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunyiweather.android.db.City;
import com.yunyiweather.android.db.County;
import com.yunyiweather.android.db.Province;
import com.yunyiweather.android.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * (遍历全国的省市县数据)
 * 自定义工具类Utility:
 * ~解析和处理服务器返回的省市县数据
 * ~解析天气JSON数据
 *
 * Created by 张云天 on 2017/4/10.
 */

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     * 使用JSONObject方式解析JSON数据
     * 步骤:1. 由于服务器中定义的是一个JSON数组,因此首先将服务器返回的数据传入了一个JSONArray对象中;
     *     2. 从JSONArray数组中取出的每一个元素都是一个JSONObject对象,故使用for循环遍历JSONArray数组
     *     3. 在循环中将JSONObject对象的各种数据取出,放在Province类的对象中
     *     4. 使用save()方法将province数据保存在数据库中
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvince = new JSONArray(response);
                for(int i = 0; i < allProvince.length(); i++)
                {
                    JSONObject provinceObject = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();//继承自DataSupport类的方法,将数据保存在数据库中
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCity = new JSONArray(response);
                for(int i = 0; i < allCity.length(); i++)
                {
                    JSONObject cityObject = allCity.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
            JSONArray allCounty = new JSONArray(response);
            for(int i = 0; i < allCounty.length(); i++)
            {
                JSONObject countyObject = allCounty.getJSONObject(i);
                County county = new County();
                county.setCountyName(countyObject.getString("name"));
                county.setWeatherId(countyObject.getString("weather_id"));
                county.setCityId(cityId);
                county.save();
            }
            return true;
        }catch (JSONException e){
                e.printStackTrace();
            }
        }
    return false;
    }
    /**
     * 将返回的JSON数据解析成Weather实体类:
     * 由于使用的是guolin精简之后的数据,所以使用JSONObject和JSONArray将天气数据中的主体内容解析出来
     * 再使用GSON方法解析JSON数据
     */
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
