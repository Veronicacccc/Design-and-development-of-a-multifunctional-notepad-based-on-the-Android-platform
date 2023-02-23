package com.example.daily;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daily.fragments.AbsorbedFragment;
import com.example.daily.fragments.MusicFragment;
import com.example.daily.fragments.TaskFragment;
import com.example.daily.fragments.WeatherFragment;
import com.example.daily.weathers.Weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final String weatherUrl = "https://yiketianqi.com/api?version=v1&appid=33699742&appsecret=BE6XbbbW";

    public List<Fragment> fragmentList = new ArrayList<>();
    private FragmentManager fragmentManager;
    private MyHandler myHandler;
    private final int mRequestCode=100;

    //需要申请权限SD读权限
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    String[] permissions=new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionlist=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 除了天气模块在MainActivity.java中，其他模块均在各自的Fragment继承类中
        //初始化底部导航栏
        InitBottomNavigation();
        //天气模块初始化
        myHandler = new MyHandler();
        RefreshWeatherData();

        initPermission();//权限判断和申请

        /*** onCreate End ***/
    }

    //权限判断和申请
    private void initPermission(){
        mPermissionlist.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for(int i = 0; i < permissions.length; i++){
            if(ContextCompat.checkSelfPermission(this,permissions[i])!= PackageManager.PERMISSION_GRANTED){
                mPermissionlist.add(permissions[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if(mPermissionlist.size() > 0){//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this,permissions,mRequestCode);
        }

    }

    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss=false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showPermissionDialog();//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
            }
        }
    }
    /**
     * 不再提示权限时的展示对话框
     */
    AlertDialog mPermissionDialog;

    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动授予")
                    .setCancelable(false)
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();

                            //跳转到设置权限界面
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                            intent.setData(uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            finish();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    //关闭对话框
    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

    //天气模块
    public void RefreshWeatherData() {

        //获取okHttpClient对象
        OkHttpClient client = new OkHttpClient();
        //创建request请求
        Request request = new Request.Builder().url(weatherUrl).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //获取具体内容
                String weatherJson = response.body().string();
                Weather weather = new Gson().fromJson(weatherJson, Weather.class);
                Message message = new Message();
                message.what = 1;
                message.obj = weather;
                myHandler.sendMessage(message);
            }
        });
    }

    public void ShowWeatherInfo(Weather weather) {
        String city = weather.getCity();
        String wea = weather.getData().get(0).getWea();
        String maxTem = weather.getData().get(0).getTem1();
        String minTem = weather.getData().get(0).getTem2();
        String tem = weather.getData().get(0).getTem();
        String humidity = "湿度           " + weather.getData().get(0).getHumidity();
        String air_level = "空气指数   " + weather.getData().get(0).getAir_level();

        // tem  tem1  tem2  city  wea  rain  pm  image
        ((TextView) findViewById(R.id.cityView)).setText(city);
        ((TextView) findViewById(R.id.weaView)).setText(wea);
        ((TextView) findViewById(R.id.mmtemView)).setText(
                String.format("%s° / %s°", minTem.substring(0, minTem.length() - 1), maxTem.substring(0, maxTem.length() - 1)));
        ((TextView) findViewById(R.id.temView)).setText(tem.substring(0, tem.length() - 1) + "°");
        ((TextView) findViewById(R.id.humidityView)).setText(humidity);
        ((TextView) findViewById(R.id.levelView)).setText(air_level);
        ShowWeatherImage(wea);

    }

    public void ShowWeatherImage(String w) {
        //天气大图片
        ImageView imageView = (ImageView) findViewById(R.id.weaImageView);
        //获取当前系统时间
        Calendar calendar;
        calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        boolean isAM = calendar.get(Calendar.AM_PM) == Calendar.PM;
        int Hour = isAM ? calendar.get(Calendar.HOUR) + 12 : calendar.get(Calendar.HOUR);
        switch (w) {
            case "暴雪":
            case "大雪":
            case "中雪":
            case "小雪":
                imageView.setImageResource(R.drawable.b_xue);
                break;
            case "暴雨":
            case "大雨":
            case "中雨":
                imageView.setImageResource(R.drawable.b_dayu);
                break;
            case "多云":
            case "多云转阴":
            case "阴":
                imageView.setImageResource(R.drawable.b_yin);
                break;
            case "多云转晴":
                if (Hour >= 18) imageView.setImageResource(R.drawable.night_qingzhuanduoyun);
                else imageView.setImageResource(R.drawable.qingzhuanduoyun);
                break;
            case "雷阵雨":
                imageView.setImageResource(R.drawable.b_leizhenyu);
                break;
            case "晴":
                if (Hour >= 18) imageView.setImageResource(R.drawable.b_nightqing);
                else imageView.setImageResource(R.drawable.b_qing);
                break;
            case "雾":
                imageView.setImageResource(R.drawable.b_wu);
                break;
            case "小雨":
            case "阵雨":
                imageView.setImageResource(R.drawable.b_xioayu);
                break;

        }
    }

    public void ReFreshWeatherImageClick(View view) {
        RefreshWeatherData();
    }

    // 底部导航栏模块
    public void InitBottomNavigation() {
        // 添加四个fragment实例到fragmentList，以便管理
        fragmentList.add(new TaskFragment());
        fragmentList.add(new AbsorbedFragment());
        fragmentList.add(new MusicFragment());
        fragmentList.add(new WeatherFragment());

        //建立fragment管理器
        fragmentManager = getSupportFragmentManager();

        //管理器开启事务，将fragment实例加入管理器
        fragmentManager.beginTransaction()
                .add(R.id.FragmentLayout, fragmentList.get(0), "TASK")
                .add(R.id.FragmentLayout, fragmentList.get(1), "ABSORBED")
                .add(R.id.FragmentLayout, fragmentList.get(2), "MUSIC")
                .add(R.id.FragmentLayout, fragmentList.get(3), "WEATHER")
                .commit();

        //设置fragment显示初始状态
        fragmentManager.beginTransaction()
                .show(fragmentList.get(0))
                .hide(fragmentList.get(1))
                .hide(fragmentList.get(2))
                .hide(fragmentList.get(3))
                .commit();

        //设置底部导航栏点击选择监听事件
        BottomNavigationView bottomNavigationView = findViewById(R.id.BottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // return true : show selected style
                // return false: do not show
                switch (item.getItemId()) {
                    case R.id.menu_task:
                        ShowFragment(0);
                        return true;
                    case R.id.menu_accounts:
                        ShowFragment(1);
                        return true;
                    case R.id.menu_absorbed:
                        ShowFragment(2);
                        return true;
                    case R.id.menu_weather:
                        ShowFragment(3);
                        return true;
                    default:
                        Log.i(TAG, "onNavigationItemSelected: Error");
                        break;
                }
                return false;
            }
        });
    }

    public void ShowFragment(int index) {
        fragmentManager.beginTransaction()
                .show(fragmentList.get(index))
                .hide(fragmentList.get((index + 1) % 4))
                .hide(fragmentList.get((index + 2) % 4))
                .hide(fragmentList.get((index + 3) % 4))
                .commit();
    }

    //消息处理类
    public class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //what == 1   天气消息
            if (msg.what == 1) {
                ShowWeatherInfo((Weather) msg.obj);
            }
        }
    }
}