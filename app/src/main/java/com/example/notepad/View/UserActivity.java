package com.example.notepad.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.notepad.Controller.ColorChangeOnTouchListener;
import com.example.notepad.Controller.HttpThread;
import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.FileUtil;
import com.example.notepad.Helper.Util;
import com.example.notepad.Model.Responce;
import com.example.notepad.R;

import java.util.HashMap;

public class UserActivity extends BaseActivity implements View.OnClickListener {
    //返回键
    private ImageView ivBack;
    //注销按钮
    private Button btnLogoff;
    //可触摸layout 昵称、邮箱、密码
    private RelativeLayout layoutName, layoutEmail, layoutPassword;
    //当前信息文本 昵称、邮箱、密码
    private TextView tvName, tvEmail, tvPassword;

    //当前弹窗
    AlertDialog dialog;
    //当前弹窗中的文本
    ClearEditText text;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        //设置标题栏颜色
        Util.setStatusColor(getWindow(), Config.colorWhite);
        //绑定
        ivBack = findViewById(R.id.back);
        btnLogoff = findViewById(R.id.logoff_btn);
        layoutName = findViewById(R.id.name);
        layoutEmail = findViewById(R.id.email);
        layoutPassword = findViewById(R.id.password);
        tvName = findViewById(R.id.name_text);
        tvEmail = findViewById(R.id.email_text);
        tvPassword = findViewById(R.id.password_text);
        //设置点击监听器
        ivBack.setOnClickListener(this);
        btnLogoff.setOnClickListener(this);
        layoutName.setOnClickListener(this);
        layoutEmail.setOnClickListener(this);
        layoutPassword.setOnClickListener(this);
        //显示用户当前信息 昵称、邮箱
        tvName.setText(FileUtil.read(Config.NAME, Util.getStringFromXML(R.string.name_title, UserActivity.this)));
        tvEmail.setText(FileUtil.read(Config.EMAIL, Util.getStringFromXML(R.string.email_title, UserActivity.this)));
    }//todo 退出登录清除文件信息

    //上次按键时间
    private long mClickTime = 0;
    //上次按键对象
    private View mView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logoff_btn://注销 清空文件并返回
                //如果有离线便签则显示确认框
                if (FileUtil.readTmp().size() > 0) {
                    this.sendAlertSureLogoff();
                } else {
                    this.logoff();
                }
                break;
            case R.id.back://返回
                this.finish();
                break;
            //弹窗
            case R.id.cancel://取消
                dialog.dismiss();
                break;
            case R.id.sure://确定
                //当前用户修改字段
                String value = text.getText() != null ? text.getText().toString() : "";
                //判断当前用户修改字段
                switch (mView.getId()) {
                    case R.id.name://修改昵称
                        //检查
                        if (Util.checkName(value)) {
                            //成功则发送请求并修改昵称
                            if (this.sendModifyPost(Config.NAME, value)) {
                                //若发送成功
                                tvName.setText(text.getText());//设置显示文本
                                FileUtil.save(Config.NAME, text.getText().toString());//存储信息
                                dialog.dismiss();
                                Util.sendToast(UserActivity.this, "昵称修改成功");
                            }
                        } else {
                            Util.sendToast(UserActivity.this, "昵称不能为空");
                            text.setShakeAnimation();
                        }
                        break;
                    case R.id.email://修改邮箱
                        if (Util.checkEmail(value)) {
                            if (this.sendModifyPost(Config.EMAIL, value)) {
                                tvEmail.setText(text.getText());
                                FileUtil.save(Config.EMAIL, text.getText().toString());
                                dialog.dismiss();
                                Util.sendToast(UserActivity.this, "邮箱修改成功");
                            }
                        } else {
                            Util.sendToast(UserActivity.this, "邮箱非法");
                            text.setShakeAnimation();
                        }
                        break;
                    case R.id.password://修改密码
                        if (Util.checkPassword(value)) {
                            if (this.sendModifyPost(Config.PASSWORD, value)) {
                                tvPassword.setText(text.getText());
                                //密码不能存储
                                //FileUtil.save(Config.PASSWORD, text.getText().toString());
                                dialog.dismiss();
                                Util.sendToast(UserActivity.this, "密码修改成功");
                            }
                        } else {
                            Util.sendToast(UserActivity.this, "密码长度需为6-50");
                            text.setShakeAnimation();
                        }
                        break;
                }
                break;
            //可触碰layout
            default:
                long mNowTime = System.currentTimeMillis();//获取第一次按键时间
                if (mView == v && (mNowTime - mClickTime) <= Config.RELAY_TIME) {
                    String strHint = "在这里输入新";
                    switch (v.getId()) {
                        case R.id.name:
                            strHint += "昵称";
                            break;
                        case R.id.email:
                            strHint += "邮箱";
                            break;
                        case R.id.password:
                            strHint += "密码";
                            break;
                    }
                    //启动弹窗
                    this.sendAlert(strHint);
                } else if (mView != v || (mNowTime - mClickTime) > Config.RELAY_TIME) {//比较两次按键时间差和按键对象
                    Util.sendToast(getApplicationContext(), "双击即可修改");
                    mClickTime = mNowTime;
                    mView = v;
                }
        }
    }

    //弹窗-修改
    @SuppressLint("ClickableViewAccessibility")
    private void sendAlert(String textHint) {
        dialog = new AlertDialog.Builder(UserActivity.this).create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setContentView(R.layout.layout_user_alert);
            window.setGravity(Gravity.CENTER);
            dialog.setCanceledOnTouchOutside(true);
            //绑定
            text = window.findViewById(R.id.text);
            text.setHint(textHint);
            Button btnCancel = window.findViewById(R.id.cancel);
            Button btnSure = window.findViewById(R.id.sure);
            //手动设置监听
            btnCancel.setOnTouchListener(new ColorChangeOnTouchListener());
            btnSure.setOnTouchListener(new ColorChangeOnTouchListener());
            btnCancel.setOnClickListener(this);
            btnSure.setOnClickListener(this);
            //弹出软键盘监听
            window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);//焦点
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);//自动弹出

            //查看网络，给出提示
            if (!Util.isNetworkConnected(UserActivity.this))
                Util.sendToast(UserActivity.this, "暂无网络连接，无法修改用户信息><");
        }
    }

    //发送修改请求 只修改key的值
    private boolean sendModifyPost(String key, String value) {
        String id = FileUtil.read(Config.ID);
        if (!id.isEmpty()) {
            //存放信息
            HashMap<String, String> msg = new HashMap<>();
            msg.put(Config.ID, id);//用户id
            msg.put(key, value);//key值
            //创建响应并发送请求 发送邮箱
            Responce responce = new Responce();
            HttpThread.startHttpThread(Config.URL_USER_MODIFY, msg, responce, UserActivity.this);
            //返回响应
            return responce.code == 0;
        }
        return false;
    }

    //弹窗-是否登出
    @SuppressLint("ClickableViewAccessibility")
    private void sendAlertSureLogoff() {
        dialog = new AlertDialog.Builder(UserActivity.this).create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setContentView(R.layout.layout_logoff_sure);
            //绑定
            Button btnCancel = window.findViewById(R.id.cancel);
            Button btnSure = window.findViewById(R.id.sure);
            //设置监听
            btnCancel.setOnTouchListener(new ColorChangeOnTouchListener());
            btnSure.setOnTouchListener(new ColorChangeOnTouchListener());
            btnCancel.setOnClickListener(new View.OnClickListener() {//取消
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            btnSure.setOnClickListener(new View.OnClickListener() {//确定
                @Override
                public void onClick(View v) {
                    logoff();
                }
            });
        }
    }

    //登出操作
    private void logoff() {
        //清空文件
        FileUtil.clear();
        //关闭提示
        if (this.dialog != null && this.dialog.isShowing())
            this.dialog.dismiss();
//        //关闭页面-在其他页面进行跳转登陆
//        this.finish();
        //没有登录信息则返回登录页面（先转主页，再自动转。否则登录后没有返回页面）且清除其他页面
        Intent intent = new Intent(UserActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);//清除其他页面
        startActivity(intent);
    }
}
