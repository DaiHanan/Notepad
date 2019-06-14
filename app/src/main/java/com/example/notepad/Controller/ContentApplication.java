package com.example.notepad.Controller;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

//初始化时启动 用于获得上下文
public class ContentApplication extends Application {
    //上下文对象
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        //获取Context
        context = getApplicationContext();
    }

    //返回
    public static Context getContext() {
        return context;
    }
}

