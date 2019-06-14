package com.example.notepad.View;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.example.notepad.Controller.ActivityContainer;
import com.example.notepad.Helper.Util;

//基础页面，用于页面栈的管理、标题栏字体颜色管理
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //标题栏字体变深
        Util.setStatusFontColorDep(getWindow());
        //添加Activity到栈
        ActivityContainer.getInstance().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //结束Activity并从栈中移除该Activity
        ActivityContainer.getInstance().removeActivity(this);
    }
}
