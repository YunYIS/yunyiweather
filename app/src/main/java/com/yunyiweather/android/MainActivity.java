package com.yunyiweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //如果用户是第一次进入程序，则开始是选择城市界面，在保存城市（用户第二次进入时，就直接加载上次保存的城市的天气信息）
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather", null) != null){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(Intent.ACTION_MAIN); //主启动,不期望接收数据
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //新的activity栈中开启,或者已经存在就调到栈前;
            intent.addCategory(Intent.CATEGORY_HOME);//添加种类,为设备首次显示的页面;
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}
