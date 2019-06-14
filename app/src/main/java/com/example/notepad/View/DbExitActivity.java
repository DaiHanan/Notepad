package com.example.notepad.View;

import android.annotation.SuppressLint;

import com.example.notepad.Controller.ActivityContainer;
import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.Util;

//双击返回退出功能基础页面
@SuppressLint("Registered")
public class DbExitActivity extends BaseActivity {
    //退出时间差
    private long mPressedTime = 0;

    //双击返回退出
    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间
        if ((mNowTime - mPressedTime) > Config.RELAY_TIME) {//比较两次按键时间差
            Util.sendToast(getApplicationContext(), "再次返回退出程序");
            mPressedTime = mNowTime;
        } else {//退出程序
            ActivityContainer.getInstance().finishAllActivity();
        }
    }
}
