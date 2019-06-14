package com.example.notepad.View;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.notepad.Controller.HttpThread;
import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.Util;
import com.example.notepad.Model.Responce;
import com.example.notepad.R;

import java.util.HashMap;

//忘记密码页面
public class ForgetActivity extends BaseActivity implements View.OnClickListener {
    //返回图标
    private ImageView ivBack;
    //发送邮箱按钮
    private Button btnSendEmail;
    //邮箱文本框
    private ClearEditText etEmail;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        //设置标题栏颜色
        Util.setStatusColor(getWindow(), Color.WHITE);
        //绑定
        ivBack = findViewById(R.id.back);
        btnSendEmail = findViewById(R.id.send_email_btn);
        etEmail = findViewById(R.id.email_text);
        //设置点击监听
        ivBack.setOnClickListener(this);
        btnSendEmail.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_email_btn://发送邮箱
                //检参
                if (etEmail.getText() != null && Util.checkEmail(etEmail.getText().toString())) {
                    //存放邮箱
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put("email", this.etEmail.getText().toString());
                    //创建响应并发送请求 发送邮箱
                    Responce responce = new Responce();
                    HttpThread.startHttpThread(Config.URL_SEND_EMAIL, msg, responce, ForgetActivity.this);
                    //处理成功响应
                    if (responce.code == 0) {
                        //保存
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(Config.FLAG, true);//成功标志
                        intent.putExtras(bundle);
                        //返回
                        setResult(RESULT_OK, intent);
                        this.finish();
                    }
                } else {
                    Util.sendToast(ForgetActivity.this, "邮箱格式非法");
                    etEmail.setShakeAnimation();
                }

                break;
            case R.id.back://返回
                this.finish();
                break;
        }
    }
}
