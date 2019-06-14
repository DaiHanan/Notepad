package com.example.notepad.View;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.Util;
import com.example.notepad.R;

import java.util.Objects;

public class AboutActivity extends BaseActivity {

    //返回图标
    ImageView ivBack;
    //点击查看
    TextView vtClick;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //设置标题栏颜色
        Util.setStatusColor(this.getWindow(), Config.colorWhite);
        //绑定
        ivBack = findViewById(R.id.back);
        vtClick = findViewById(R.id.click_to_see);
        //监听器
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        vtClick.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(AboutActivity.this).show();
                alertDialog.setContentView(
                        LayoutInflater.from(AboutActivity.this).inflate(R.layout.layout_author_icon_alert, null));
                Objects.requireNonNull(alertDialog.getWindow()).setLayout(
                        Config.AUTHOR_ICON_WIDTH, LinearLayout.LayoutParams.WRAP_CONTENT);
            }
        });
    }
}
