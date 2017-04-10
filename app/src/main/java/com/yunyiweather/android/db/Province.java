package com.yunyiweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * (db包下存放数据库模型相关代码)
 *
 * 数据库中的province表,对应的实体类Province
 *
 * (LitePal的每一个实体类都应该继承DataSupport类,在使用LitePal进行表管理的时候不需要模型类有任何的继承结构,
 * 但是进行CRUD操作的时候就必须继承DataSupport类才行)
 * Created by 张云天 on 2017/4/10.
 */

public class Province extends DataSupport{

    private int id;//id是每一个实体类中都应有的字段(数据库自动生成)
    private String provinceName;
    private int provinceCode;//记录省的代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
