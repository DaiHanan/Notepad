package com.example.notepad.View;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;

import com.example.notepad.Controller.HttpThread;
import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.Util;
import com.example.notepad.Model.Responce;
import com.example.notepad.R;

import java.util.HashMap;

public class LoginActivity extends DbExitActivity implements View.OnClickListener {
    //按钮：登录、注册、忘记密码
    private Button btnLogin, btnRegister, btnForget;
    //文本框：邮箱，密码
    private ClearEditText etEmail, etPassword;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //设置状态栏颜色
        Util.setStatusColor(this.getWindow(), Config.colorWhite);
        //绑定
        btnLogin = findViewById(R.id.login_btn);
        btnRegister = findViewById(R.id.register_btn);
        btnForget = findViewById(R.id.forget_password_btn);
        etEmail = findViewById(R.id.email_text);
        etPassword = findViewById(R.id.password_text);
        //监听器
        //点击监听器
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnForget.setOnClickListener(this);
//        //触摸监听器
//        btnRegister.setOnTouchListener(this);
//        btnForget.setOnTouchListener(this);

    }

    //点击事件
    @Override
    public void onClick(View v) {
        //事件和包裹
        Intent intent;
        Bundle bundle;
        switch (v.getId()) {
            case R.id.login_btn://登录
                //检查邮箱格式
                if (this.checkLoginInfo()) {
                    //存放邮箱和密码
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put(Config.EMAIL, this.etEmail.getText() != null ? this.etEmail.getText().toString() : "");
                    msg.put(Config.PASSWORD, this.etPassword.getText() != null ? this.etPassword.getText().toString() : "");
                    //创建响应并发送请求 登录
                    Responce responce = new Responce();
                    HttpThread.startHttpThread(Config.URL_LOGIN, msg, responce, LoginActivity.this);
                    //处理成功响应
                    if (responce.code == 0) {
                        //保存
                        intent = new Intent();
                        bundle = new Bundle();
                        bundle.putBoolean(Config.FLAG, true);
                        bundle.putInt("id", responce.user.id);
                        bundle.putString(Config.NAME, responce.user.name);
                        bundle.putString(Config.EMAIL, responce.user.email);
                        intent.putExtras(bundle);
                        //返回
                        setResult(RESULT_OK, intent);
                        this.finish();
                    }
                }
                break;
            case R.id.register_btn://注册
                startActivityForResult(
                        new Intent(LoginActivity.this, RegisterActivity.class),
                        Config.CODE_REGISTER
                );
                break;
            case R.id.forget_password_btn://忘记密码
                startActivityForResult(
                        new Intent(LoginActivity.this, ForgetActivity.class),
                        Config.CODE_FORGET_EMAIL
                );
                break;
            case R.id.back://返回
                this.finish();
                break;
        }
    }

    //检查登录信息
    private boolean checkLoginInfo() {
        if (this.etEmail.getText() == null || !Util.checkEmail(this.etEmail.getText().toString())) {//邮箱错误
            Util.sendToast(LoginActivity.this, "邮箱格式非法");
            this.etEmail.setShakeAnimation();
        } else if (this.etPassword.getText() == null || !Util.checkPassword(this.etPassword.getText().toString())) {//密码错误，6-50位
            Util.sendToast(LoginActivity.this, "密码长度需为6-50位");
            this.etPassword.setShakeAnimation();
        } else {
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK &&
                data != null &&
                data.getExtras() != null &&
                data.getExtras().getBoolean(Config.FLAG, false)) {
            switch (requestCode) {
                case Config.CODE_REGISTER://注册
                    //填充邮箱
                    this.etEmail.setText(data.getExtras().getString(Config.EMAIL));

                    Util.sendToast(LoginActivity.this, "注册成功");
                    break;
                case Config.CODE_FORGET_EMAIL://忘记密码
                    Util.sendToast(LoginActivity.this, "邮件已发送，请前往重置密码");
                    break;
            }
        }
    }
}
