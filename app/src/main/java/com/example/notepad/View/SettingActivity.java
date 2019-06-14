package com.example.notepad.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.notepad.Controller.ColorChangeOnTouchListener;
import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.FileUtil;
import com.example.notepad.Helper.Util;
import com.example.notepad.R;

import java.util.ArrayList;

public class SettingActivity extends BaseActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    //可触碰layout 文字大小、排序方式、查看个人信息、快捷删除、关于
    RelativeLayout layoutFontSize, layoutOrderType, layoutCheckInfo, layoutQuickDel, layoutAbout;
    //返回图标
    ImageView btnBack;
    //当前样式 文字大小、排序方式、快捷删除
    TextView tvNowFontSize, tvNowOrderType;
    Switch switchQuickDel;

    //当前显示的弹窗
    AlertDialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //设置状态栏颜色
        Util.setStatusColor(getWindow(), Config.colorWhite);
        //绑定
        layoutFontSize = findViewById(R.id.font_size);
        layoutOrderType = findViewById(R.id.order_type);
        layoutCheckInfo = findViewById(R.id.check_user_info);
        layoutQuickDel = findViewById(R.id.quick_delete);
        layoutAbout = findViewById(R.id.about);
        btnBack = findViewById(R.id.back);
        tvNowFontSize = findViewById(R.id.font_size_now);
        tvNowOrderType = findViewById(R.id.order_type_now);
        switchQuickDel = findViewById(R.id.quick_delete_switch);
        //读取文件获得当前设置并刷新
        String strNowFontSize = FileUtil.read(Config.NOW_FONT_SIZE);
        String strNowOrderType = FileUtil.read(Config.NOW_ORDER_TYPE);
        String strNowQuickDel = FileUtil.read(Config.NOW_QUICK_DEL);
        tvNowFontSize.setText(
                Config.ARR_FONT_SIZE_NAME[
                        strNowFontSize.equals("")
                                ? Config.DEFAULT_FONT_SIZE_INDEX
                                : Integer.parseInt(strNowFontSize)
                        ]);
        tvNowOrderType.setText(
                Config.ARR_ORDER_TYPE[
                        strNowOrderType.equals("")
                                ? Config.DEFAULT_ORDER_TYPE_INDEX
                                : Integer.parseInt(strNowOrderType)
                        ]);
        switchQuickDel.setChecked(strNowQuickDel.equals(Config.YES));
        //设置点击监听
        btnBack.setOnClickListener(this);
        layoutFontSize.setOnClickListener(this);
        layoutOrderType.setOnClickListener(this);
        layoutCheckInfo.setOnClickListener(this);
        layoutQuickDel.setOnClickListener(this);
        layoutAbout.setOnClickListener(this);
        //switch监听
        switchQuickDel.setOnCheckedChangeListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back://返回
                this.finish();
                break;
            //字体大小弹窗
            case R.id.font_size://切换字体 弹窗
                this.sendAlertFontSize();
                break;
            case R.id.small://字体-小
                //修改文件
                FileUtil.save(Config.NOW_FONT_SIZE, "0");
                //更新设置界面显示
                this.tvNowFontSize.setText(Config.ARR_FONT_SIZE_NAME[0]);
                dialog.dismiss();
                break;
            case R.id.normal:
                FileUtil.save(Config.NOW_FONT_SIZE, "1");
                this.tvNowFontSize.setText(Config.ARR_FONT_SIZE_NAME[1]);
                dialog.dismiss();
                break;
            case R.id.big:
                FileUtil.save(Config.NOW_FONT_SIZE, "2");
                this.tvNowFontSize.setText(Config.ARR_FONT_SIZE_NAME[2]);
                dialog.dismiss();
                break;
            case R.id.sup:
                FileUtil.save(Config.NOW_FONT_SIZE, "3");
                this.tvNowFontSize.setText(Config.ARR_FONT_SIZE_NAME[3]);
                dialog.dismiss();
                break;
            //切换排序方式
            case R.id.order_type:
                this.sendAlertOrderType();
                break;
            case R.id.create://排序方式-创建
                //修改文件
                FileUtil.save(Config.NOW_ORDER_TYPE, "0");
                //更新设置界面显示
                this.tvNowOrderType.setText(Config.ARR_ORDER_TYPE[0]);
                dialog.dismiss();
                break;
            case R.id.modify:
                FileUtil.save(Config.NOW_ORDER_TYPE, "1");
                this.tvNowOrderType.setText(Config.ARR_ORDER_TYPE[1]);
                dialog.dismiss();
                break;
            case R.id.cancel://弹窗取消
                dialog.dismiss();
                break;

            case R.id.check_user_info://检查个人信息
                startActivity(new Intent(SettingActivity.this, UserActivity.class));
                break;
            case R.id.quick_delete://快捷删除
                //反向设置switch
                switchQuickDel.setChecked(!FileUtil.read(Config.NOW_QUICK_DEL).equals("1"));
                break;
            case R.id.about://关于
                startActivity(new Intent(SettingActivity.this, AboutActivity.class));
                break;
        }
    }

    //弹窗-字体大小
    private void sendAlertFontSize() {
        //创建提示框
        dialog = new AlertDialog.Builder(SettingActivity.this).create();
        dialog.show();
        //获得窗体
        Window window = dialog.getWindow();
        if (window != null) {
            //设置布局
            window.setContentView(R.layout.layout_font_size_alert);
            //设置位置及点击组件外效果
            window.setGravity(Gravity.BOTTOM);
            dialog.setCanceledOnTouchOutside(true);
            //绑定
            ArrayList<View> layouts = new ArrayList<>();
            layouts.add(window.findViewById(R.id.small));
            layouts.add(window.findViewById(R.id.normal));
            layouts.add(window.findViewById(R.id.big));
            layouts.add(window.findViewById(R.id.sup));
            layouts.add(window.findViewById(R.id.cancel));
            //手动设置监听
            for (View view : layouts) {
                view.setOnTouchListener(new ColorChangeOnTouchListener());
                view.setOnClickListener(this);
            }
            //根据当前设置显示图标
            switch (Integer.parseInt(FileUtil.read(Config.NOW_FONT_SIZE, Config.DEFAULT_FONT_SIZE_INDEX + ""))) {
                case 0:
                    window.findViewById(R.id.icon_0).setVisibility(View.VISIBLE);
                    break;
                case 1:
                    window.findViewById(R.id.icon_1).setVisibility(View.VISIBLE);
                    break;
                case 2:
                    window.findViewById(R.id.icon_2).setVisibility(View.VISIBLE);
                    break;
                case 3:
                    window.findViewById(R.id.icon_3).setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    //弹窗-排序方式
    private void sendAlertOrderType() {
        dialog = new AlertDialog.Builder(SettingActivity.this).create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setContentView(R.layout.layout_order_type_alert);
            window.setGravity(Gravity.BOTTOM);
            dialog.setCanceledOnTouchOutside(true);
            //绑定
            ArrayList<View> layouts = new ArrayList<>();
            layouts.add(window.findViewById(R.id.create));
            layouts.add(window.findViewById(R.id.modify));
            layouts.add(window.findViewById(R.id.cancel));
            //手动设置监听
            for (View view : layouts) {
                view.setOnTouchListener(new ColorChangeOnTouchListener());
                view.setOnClickListener(this);
            }
            //根据当前设置显示图标
            switch (Integer.parseInt(FileUtil.read(Config.NOW_ORDER_TYPE, Config.DEFAULT_ORDER_TYPE_INDEX + ""))) {
                case 0:
                    window.findViewById(R.id.icon_0).setVisibility(View.VISIBLE);
                    break;
                case 1:
                    window.findViewById(R.id.icon_1).setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    //switch改变监听
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //修改文件
        FileUtil.save(Config.NOW_QUICK_DEL, isChecked ? "1" : "0");
    }

    @Override
    protected void onResume() {
        super.onResume();

//        //检查登录状态，不存在返回登录界面(注销时可能)
//        if (!FileUtil.read(Config.LOGIN).equals(Config.YES)) {
//            //没有登录信息则返回登录页面（先转主页，再自动转。否则登录后没有返回页面）且清除其他页面
//            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);//清除其他页面
//            startActivity(intent);
//        }
    }

}
