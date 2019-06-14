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

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    //注册键
    private Button btnRegister;
    //返回图标
    private ImageView ivBack;
    //输入框 昵称、邮箱、密码
    private ClearEditText etName, etEmail, etPassword;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //设置标题栏颜色
        Util.setStatusColor(getWindow(), Color.WHITE);
        //绑定
        ivBack = findViewById(R.id.back);
        btnRegister = findViewById(R.id.register_btn);
        etName = findViewById(R.id.name_text);
        etEmail = findViewById(R.id.email_text);
        etPassword = findViewById(R.id.password_text);
        //设置点击监听
        ivBack.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_btn://注册键
                //检查通过后
                if (this.checkRegisterInfo()) {
                    //存放昵称、邮箱和密码
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put("name", this.etName.getText() != null ? this.etName.getText().toString() : "");
                    msg.put("email", this.etEmail.getText() != null ? this.etEmail.getText().toString() : "");
                    msg.put("password", this.etPassword.getText() != null ? this.etPassword.getText().toString() : "");
                    //创建响应并发送请求 发送邮箱
                    Responce responce = new Responce();
                    HttpThread.startHttpThread(Config.URL_REGISTER, msg, responce, RegisterActivity.this);
                    //处理成功响应
                    if (responce.code == 0) {
                        //保存
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(Config.FLAG, true);
                        bundle.putString("email", this.etEmail.getText().toString());//填充邮箱
                        intent.putExtras(bundle);
                        //返回
                        setResult(RESULT_OK, intent);
                        this.finish();
                    }
                }
                break;
            case R.id.back://返回按钮
                this.finish();
                break;
        }
    }

    //检查注册信息
    private boolean checkRegisterInfo() {
        if (!Util.checkName(this.etName.getText() != null ? this.etName.getText().toString() : "")) {//昵称错误
            Util.sendToast(RegisterActivity.this, "请输入个性昵称");
            this.etName.setShakeAnimation();
        } else if (!Util.checkEmail(this.etEmail.getText() != null ? this.etEmail.getText().toString() : "")) {//邮箱错误
            Util.sendToast(RegisterActivity.this, "邮箱格式非法");
            this.etEmail.setShakeAnimation();
        } else if (!Util.checkPassword(this.etPassword.getText() != null ? this.etPassword.getText().toString() : "")) {//密码错误，6-50位
            Util.sendToast(RegisterActivity.this, "密码长度需为6-50位");
            this.etPassword.setShakeAnimation();
        } else {
            return true;
        }

        return false;
    }
}
