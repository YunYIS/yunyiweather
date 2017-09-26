package com.yunyiweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yunyiweather.android.gson.Forecast;
import com.yunyiweather.android.gson.Weather;
import com.yunyiweather.android.service.AutoUpdateService;
import com.yunyiweather.android.util.HttpUtil;
import com.yunyiweather.android.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.view.View.GONE;

public class WeatherActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private WeatherPagerAdapter adapter;

    private List<View> viewList = new ArrayList<>();//adapter数据源

    private String mWeatherId;
    public DrawerLayout drawerLayout;
    //************************************************************************
    private LayoutInflater inflater;//装载每个天气界面的view
    private View weather_view;//指向当前显示在屏幕中的城市的天气界面的View

    private String[] weatherInfos;

    //各布局控件
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    private Button navButton;//显示侧滑菜单按钮

    private ScrollView nowScrollView;//指向当前界面的ScrollView


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.viewpager_weather);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.swiperefresh);
        //初始化布局装载器
        inflater = LayoutInflater.from(this);
        //viewPager操作
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        //设置适配器
        adapter = new WeatherPagerAdapter(viewList);

        viewPager.setAdapter(adapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String weatherString = prefs.getString("weather", null);//通过键值来获得SharedPreferences对象中存储的数据
        if(weatherString != null && weatherString != ""){
            //有缓存时直接解析天气数据
            weatherInfos= weatherString.split("\\*");
            for(int i = 0; i < weatherInfos.length; i++){
                Weather weather = Utility.handleWeatherResponse(weatherInfos[i]);
                showWeatherInfoArray(weather);
            }
        }else if(weatherString == ""){
            navButton.setVisibility(GONE);
            drawerLayout.openDrawer(GravityCompat.START);//让DrawerLayout菜单显示出来
        }else{
            //无缓存时去服务器查询天气（第一次使用时肯定是没有缓存的，因此我们从Intent中读出weather_id）
            mWeatherId = getIntent().getStringExtra("weather_id");
            navButton.setVisibility(View.VISIBLE);
            addPager();//先拿到weatherLayout的实例
            //请求数据时项目先将ScrollView隐藏，不然空的数据界面会很奇怪
            weatherLayout.setVisibility(View.INVISIBLE);

            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new MyswipeRefresh.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //if(weatherInfos == null || weatherInfos[0] == ""){
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                    String weatherString = prefs.getString("weather", null);//通过键值来获得SharedPreferences对象中存储的数据
                    weatherInfos= weatherString.split("\\*");
//                    Log.i("swiperefresh", "if");
                //}
                Weather weather = Utility.handleWeatherResponse(weatherInfos[viewPager.getCurrentItem()]);
                Log.i("swiperefresh", "name:"+weather.basic.cityName);
                Log.i("swiperefresh", "index:"+viewPager.getCurrentItem());
                mWeatherId = weather.basic.weatherId;
                for(int i = 0; i < weatherInfos.length; i++){
                Log.i("swiperefresh", "requestWeather "+i+":" + weatherInfos[i]);}
                Log.i("swiperefresh", "mWeatherId:"+mWeatherId);
                requestWeather(mWeatherId);
            }
        });

        //尝试从SharedPreferences读取缓存图片
        String bingPic = prefs.getString("bing_pic", null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //菜单
                PopupMenu popup = new PopupMenu(WeatherActivity.this,navButton);
                //加载菜单布局
                popup.getMenuInflater().inflate(R.menu.options, popup.getMenu());
                //监听
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.add:
                                navButton.setVisibility(GONE);
                                drawerLayout.openDrawer(GravityCompat.START);//让DrawerLayout菜单显示出来
                                break;
                            case R.id.delete:
                                int position = viewPager.getCurrentItem();
                                viewList.remove(position);
                                adapter.notifyDataSetChanged();
                                viewPager.setCurrentItem(viewPager.getCurrentItem(),false);
                                if(viewList.size() != 0)
                                    weather_view = viewList.get(viewPager.getCurrentItem());
                                /**
                                 * 删除SharedPreferences的缓存
                                 */
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                                if(prefs.getString("weather", null) != null){
                                    weatherInfos = prefs.getString("weather", null).split("\\*");
                                    String save = "";
                                    for(int i = 0; i < weatherInfos.length; i++){
                                        if(i != position){
                                            save += weatherInfos[i]+"*";
                                        }
                                    }

                                    SharedPreferences.Editor editor = PreferenceManager
                                            .getDefaultSharedPreferences(WeatherActivity.this).edit();
                                    editor.putString("weather", save);
                                    /**
                                     * sharedpreferences的同步提交问题：
                                     */
                                    editor.commit();
                                }
                                //viewpager被删除完，显示选择城市的侧滑菜单
                                if(viewList.size() == 0){
                                    navButton.setVisibility(GONE);
                                    drawerLayout.openDrawer(GravityCompat.START);//让DrawerLayout菜单显示出来
                                    weather_view = null;
                                }
                                break;
                            case R.id.about:
                                AlertDialog.Builder builder = new AlertDialog.Builder(WeatherActivity.this);
                                builder.setTitle("  关于...");//设置标题
                                builder.setIcon(R.mipmap.logo);//设置图标
                                builder.setMessage("学习之作");//设置对话框内容
                                builder.setNegativeButton("返回", null);
                                AlertDialog dialog = builder.create();//获取一个对话框
                                dialog.show();//显示对话框
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        /**
         * 监听ViewPager的滑动，用于隐藏全局设置按钮
         */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            //此方法是页面跳转完后被调用，arg0是你当前选中的页面的Position（位置编号） 
            @Override
            public void onPageSelected(int position) {
                weather_view = viewList.get(position);
                widgetInit(weather_view);

                Log.i("viewpager" , "index:"+viewPager.getCurrentItem()+"");
//                if(weatherInfos == null || weatherInfos[0] == ""){
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                    String weatherString = prefs.getString("weather", null);//通过键值来获得SharedPreferences对象中存储的数据
                    weatherInfos= weatherString.split("\\*");
               // }
                //Weather weather = Utility.handleWeatherResponse(weatherInfos[viewPager.getCurrentItem()]);
                //Log.i("viewpager", "name:"+weather.basic.cityName);

                for(int i = 0; i < weatherInfos.length; i++){
                    Log.i("viewpager", "requestWeather "+i+":" + weatherInfos[i]);}

                ScrollListener();
            }
            /*
            此方法是在状态改变的时候调用。 
               其中arg0这个参数有三种状态（0，1，2） 
               arg0 ==1的时表示正在滑动，arg0==2的时表示滑动完毕了，arg0==0的时表示什么都没做 
               当页面开始滑动的时候，三种状态的变化顺序为1-->2-->0 */
            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == 1){
                    navButton.setVisibility(View.GONE);
                }
                if(state == 0){
                    navButton.setVisibility(View.VISIBLE);
                }
            }
        });
        /**
         * 监听DrawerLayout的滑动，用于隐藏全局设置按钮
         */
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                navButton.setVisibility(View.GONE);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                navButton.setVisibility(View.GONE);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                navButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        //每一次进入，默认显示第一个城市的天气
        weather_view = viewList.get(0);
        viewPager.setCurrentItem(0, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //解决swipeRefreshLayout与ScrollView冲突的问题
        ScrollListener();
    }

    /**
     * 解决swipeRefreshLayout与ScrollView冲突的问题
     */
    public void ScrollListener(){
        if(weather_view != null){
            nowScrollView = (ScrollView) weather_view.findViewById(R.id.weather_layout);
            nowScrollView.getViewTreeObserver().addOnScrollChangedListener(new  ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    swipeRefresh.setEnabled(nowScrollView.getScrollY()==0);
                }
            });
        }
    }
    /**
     * 从缓存中显示天气信息（从而封装该方法）
     */
    public void showWeatherInfoArray(Weather w){
        addPager();
        showWeatherInfo(w);
    }

    /**
     * 封装了方法（其作用为：将当前的ViewPager页的View中的控件与weatherActivity的控件变量一一对应起来）
     * @param view
     */
    public void widgetInit(View view){

        weatherLayout = (ScrollView) view.findViewById(R.id.weather_layout);
        titleCity = (TextView) view.findViewById(R.id.title_city);
        titleUpdateTime = (TextView) view.findViewById(R.id.title_update_time);
        degreeText = (TextView) view.findViewById(R.id.degree_text);
        weatherInfoText = (TextView) view.findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) view.findViewById(R.id.forecast_layout);
        pm25Text = (TextView) view.findViewById(R.id.pm25_text);
        aqiText = (TextView) view.findViewById(R.id.aqi_text);
        comfortText = (TextView) view.findViewById(R.id.comfort_text);
        carWashText = (TextView) view.findViewById(R.id.car_wash_text);
        sportText = (TextView) view.findViewById(R.id.sport_text);
    }

    /**
     * 添加天气界面
     */
    public void addPager(){

        View view = inflater.inflate(R.layout.activity_weather,null);

        widgetInit(view);

        viewList.add(view);
        adapter.notifyDataSetChanged();

    }

    /**
     * 封装以不同方式进入requestWeather()方法查询数据的方法
     * @param weatherId
     */
    public void requestWeatherMorePager(final String weatherId){
        navButton.setVisibility(View.VISIBLE);
        addPager();
        requestWeather(weatherId);
        viewPager.setCurrentItem(viewList.size()-1, false);
        weather_view = viewList.get(viewList.size()-1);
    }
    /**
     * 根据天气id请求城市的天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        //使用参数weatherId和我们申请的APIkey拼接出一个接口地址
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=" +
                "bc0418b57b2d4918819d3974ac1285d9";
        /**
         * 调用HttpUtil.sendOkHttpRequest()方法（该方法为自己封装的操作OkHttp的方法）向该地址发出请求，
         * 服务器会将响应的天气信息一JSON格式的数据返回
         */
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT)
                                .show();
                        swipeRefresh.setRefreshing(false);
                    }
                });

            }
            //回调
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                /**
                 * 服务器返回一个Response实例对象，通过String responseData = response.body().string();
                 * 来获取数据
                 */
                final String responseText = response.body().string();
                //将返回的JSON数据处理成Weather对象
                final Weather weather = Utility.handleWeatherResponse(responseText);
                //将当前线程切换到主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //服务器返回的status状态是ok，说明请求天气成功
                        if(weather != null && "ok".equals(weather.status)){
                            /**
                             * SharedPreferences存储:
                             * 使用键值对的方式来存储数据;支持多种数据类型存储(存储的是一个字符串,读取出来的还是一个字符串)
                             * 当前用法:
                             * 1. 使用PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                             *    得到了一个SharedPreferences对象;
                             * 2. 调用SharedPreferences对象的edit()方法获取一个SharedPreferences.Editor对象;
                             * 3. 向SharedPreferences.Editor对象(即editor)中添加数据(这里添加了一个字符串数据);
                             * 4. 调用commit()方法将添加的数据提交,完成数据存储.
                             */
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            String save = "";
                            if(prefs.getString("weather", null) != null){
                                save = prefs.getString("weather", null) + responseText + "*";
                            }else{
                                save = responseText+"*";
                            }
                            editor.putString("weather", save);
                            //解决同步问题（commit()将数据立刻提交到磁盘中）
                            boolean b = editor.commit();
                            Log.i("requsetWeather commit:", ""+b);
//                            Log.i("requsetWeather:", save);
                            //显示内容
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT)
                                    .show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });

    }
    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                        (WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });

    }
    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;
        String updateTime = "更新时间："+weather.basic.update.updateTime.split(" ")[1];//split() 方法用于把一个字符串分割成字符串数组
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast: weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temerature.max);
            minText.setText(forecast.temerature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动指数: " + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

}


