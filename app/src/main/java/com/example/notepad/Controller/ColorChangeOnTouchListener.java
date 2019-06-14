package com.example.notepad.Controller;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.Util;

//选择框触碰监听器 修改背景颜色
public class ColorChangeOnTouchListener implements View.OnTouchListener {
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //颜色变化效果
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setBackgroundColor(Config.colorPrimary);
                break;
            case MotionEvent.ACTION_UP:
                v.setBackgroundColor(Config.colorWhite);
                break;
            case MotionEvent.ACTION_MOVE:
                //移出时颜色变回
                if (!Util.isTouchPointInView(v, event.getRawX(), event.getRawY())) {
                    v.setBackgroundColor(Config.colorWhite);
                }
                break;
        }

        return false;
    }
}
