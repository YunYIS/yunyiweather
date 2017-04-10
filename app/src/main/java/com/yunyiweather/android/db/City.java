package com.yunyiweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * 数据库中的city表,对应的实体类City
 * Created by 张云天 on 2017/4/10.
 */

public class City extends DataSupport{

    private int id;
    private String cityName;
    private int cityCode;
    private int provinceId;//记录当前市所属省的id(应该指的是数据库中的id,而不是服务器中的id)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

}
