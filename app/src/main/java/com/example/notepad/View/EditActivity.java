package com.example.notepad.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.notepad.Controller.HttpThread;
import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.FileUtil;
import com.example.notepad.Helper.Util;
import com.example.notepad.Model.Responce;
import com.example.notepad.R;

import java.util.Date;
import java.util.HashMap;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("ALL")
public class EditActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {
    //文字框 时间、字数
    private TextView tvTime, tvCount;
    //便签内容
    private EditText etText;
    //图标 返回、确认、撤销、重做
    private ImageView ivBack, ivSure, ivCancel, ivUndo;

    //当前便签id
    private int id = -1;
    //创建时间（新建用）
    private Date createTime;
    //保存前便签内容
    private String oldText;
    //是否保存过
    private boolean isSaved = false;

    //撤销重置功能
    //是否为代码操作
    private boolean isAuto = false;
    //撤销堆栈
    private Stack<String> cancelStack;
    //重做堆栈
    private Stack<String> undoStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        //设置状态栏颜色
        Util.setStatusColor(getWindow(), Config.colorWhite);
        //绑定
        this.tvTime = findViewById(R.id.time);
        this.tvCount = findViewById(R.id.count);
        this.etText = findViewById(R.id.text);
        this.ivBack = findViewById(R.id.back);
        this.ivSure = findViewById(R.id.sure);
        this.ivCancel = findViewById(R.id.cancel);
        this.ivUndo = findViewById(R.id.undo);
        //监听器
        this.ivBack.setOnClickListener(this);
        this.ivSure.setOnClickListener(this);
        this.ivCancel.setOnClickListener(this);
        this.ivUndo.setOnClickListener(this);
        this.ivCancel.setOnLongClickListener(this);
        this.ivUndo.setOnLongClickListener(this);
        //设置图标不可操作（必须在setOnClickListener之后设置，否则会被覆盖）
        this.ivSure.setClickable(false);
        this.ivCancel.setClickable(false);
        this.ivUndo.setClickable(false);
        //初始化数组
        this.cancelStack = new Stack<>();
        this.undoStack = new Stack<>();
        //获得传递的参数
        Intent intent = getIntent();
        if (intent.getExtras() == null) {//新建
            //时间
            this.createTime = new Date();
            tvTime.setText(Util.parseDatetoString(this.createTime));
            //内容
            if (!Util.isNetworkConnected(EditActivity.this)) {//当没有网络时显示功能提示
                Util.sendSnackBar(
                        EditActivity.this.getWindow().getDecorView(),
                        "提供离线编辑功能，将会在有网络时自动上传^.^");
            } else {//有网络时弹出软键盘
                new Timer().schedule(
                        new TimerTask() {
                            //弹出软键盘，直接弹出会被刷新掉。延时保证页面刷新完成
                            public void run() {
                                InputMethodManager inputManager =
                                        (InputMethodManager) etText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputManager.showSoftInput(etText, 0);
                            }
                        },
                        Config.SOFT_INPUT_INTERVAL);
            }
        } else {
            Bundle bundle = intent.getExtras();
            //当前便签id
            this.id = Integer.parseInt(bundle.getString(Config.ID, "-1"));
            //时间
            tvTime.setText(bundle.getString(Config.TIME));
            //文本
            this.isAuto = true;
            this.etText.setText(bundle.getString(Config.TEXT));
            this.isAuto = false;
            //字数
            String strLength = this.etText.length() + "";
            tvCount.setText(strLength);
        }

        //设置字体 -单位：sp
        this.etText.setTextSize(
                Config.ARR_FONT_SIZE[
                        Integer.parseInt(
                                FileUtil.read(
                                        Config.NOW_FONT_SIZE,
                                        Config.DEFAULT_FONT_SIZE_INDEX + ""))]);

        //设置文本框监听器
        this.etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //只监听用户的操作
                if (!isAuto) {
                    //刷新重做栈
                    undoStack.clear();
                    setViewEnable(ivUndo, false);
                    //刷新撤销栈
                    cancelStack.push(etText.getText().toString());
                    setViewEnable(ivCancel, true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //更新当前字数
                String strLength = s.length() + "";
                tvCount.setText(strLength);
                if (!isAuto) {
                    //显示隐藏确认图标
                    if (oldText.equals(s.toString())) {//如果和修改前相同，隐藏确认
                        setViewEnable(ivSure, false);
                    } else if (!ivSure.isClickable()) {//不同时若隐藏则显示
                        setViewEnable(ivSure, true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //检查当前重做撤销总次数
                if(!isAuto && cancelStack.size() + undoStack.size() > Config.CANCEL_UNDO_COUNT) {
                    //超出则丢弃最早一次记录
                    cancelStack.remove(0);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back://返回键
                this.finish();
                break;
            case R.id.sure://确认键
                this.isSaved = true;
                this.oldText = etText.getText().toString();
                this.setViewEnable(this.ivSure, false);
                //隐藏软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                //重置撤销和重做
                this.cancelStack.clear();
                this.undoStack.clear();
                setViewEnable(this.ivCancel, false);
                setViewEnable(this.ivUndo, false);
                break;
            case R.id.cancel://撤销键
                if (!this.cancelStack.empty()) {
                    //当前文本入重做栈
                    this.undoStack.push(this.etText.getText().toString());
                    //显示重做图标
                    this.setViewEnable(this.ivUndo, true);
                    //覆盖之前文本
                    this.isAuto = true;
                    this.etText.setText(this.cancelStack.pop());
                    this.isAuto = false;
                    //设置光标位置到最后
                    this.etText.setSelection(this.etText.getText().length());
                    if (this.cancelStack.empty()) {//不可再撤销
                        //隐藏图标
                        this.setViewEnable(this.ivCancel, false);
                    }
                }
                break;
            case R.id.undo://重做键
                if (!this.undoStack.empty()) {
                    this.cancelStack.push(this.etText.getText().toString());
                    this.setViewEnable(this.ivCancel, true);
                    this.isAuto = true;
                    this.etText.setText(this.undoStack.pop());
                    this.isAuto = false;
                    this.etText.setSelection(this.etText.getText().length());
                    if (this.undoStack.empty()) {
                        this.setViewEnable(this.ivUndo, false);
                    }
                }
                break;
        }
    }

    //上传服务器
    private boolean saveText() {
        String text = etText.getText().toString();
        //如果保存过或者与最近一次保存之前的文本内容不同则上传
        if (this.isSaved || !this.oldText.equals(text)) {
            HashMap<String, String> msg = new HashMap<>();
            //当文本变动时保存服务器
            if (!text.equals("")) {//非空则新添或修改
                msg.put(Config.TEXT, text);//内容
                msg.put(Config.USER_ID, FileUtil.read(Config.ID));//用户id
                if (this.id != -1)//修改时
                    msg.put(Config.ID, this.id + "");//便签id
                //创建响应并发送请求 新添或修改
                Responce responce = new Responce();
                HttpThread.startHttpThread(Config.URL_NOTE_MODIFY, msg, responce, EditActivity.this);
                //处理成功响应
                return responce.code == 0;
            } else if (this.id > 0) {//如果内容为空且为修改时删除便签
                msg.put(Config.USER_ID, FileUtil.read(Config.ID));//用户id
                msg.put(Config.ID, this.id + "");//便签id
                //创建响应并发送请求 新添或修改
                Responce responce = new Responce();
                HttpThread.startHttpThread(Config.URL_NOTE_DEL, msg, responce, EditActivity.this);
                //处理成功响应
                return responce.code == 0;
            }
        }

        return true;
    }

    //设置图标是否显示和修改
    private void setViewEnable(View view, boolean isEnable) {
        if (isEnable && !view.isClickable()) {
            view.setVisibility(View.VISIBLE);
            view.setClickable(true);
        } else if (!isEnable && view.isClickable()) {
            view.setVisibility(View.INVISIBLE);
            view.setClickable(false);
        }
    }

    @Override
    protected void onResume() {
        //设置为初始状态
        this.oldText = this.etText.getText().toString();
        this.setViewEnable(this.ivSure, false);

        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onPause() {
        //保存文本
        if (!this.saveText()) {//保存失败则暂存本地
            FileUtil.addTmp(this.id, this.etText.getText().toString());
            Util.sendToast(EditActivity.this, "上传失败，将会在有网络的时候再次上传");
        }

        super.onPause();
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.cancel://撤销
                //清空撤销栈并依次入重做栈
                while(true) {
                    String str = this.cancelStack.pop();
                    this.undoStack.push(str);
                    if(this.cancelStack.empty()) {//最后一个元素，覆盖当前文本
                        this.isAuto = true;
                        this.etText.setText(str);
                        this.isAuto = false;
                        this.etText.setSelection(this.etText.getText().length());
                        break;
                    }
                }
                //图标刷新
                this.setViewEnable(this.ivCancel, false);
                this.setViewEnable(this.ivUndo, true);
                break;
            case R.id.undo://重做
                while(true) {
                    String str = this.undoStack.pop();
                    this.cancelStack.push(str);
                    if(this.undoStack.empty()) {
                        this.isAuto = true;
                        this.etText.setText(str);
                        this.isAuto = false;
                        this.etText.setSelection(this.etText.getText().length());
                        break;
                    }
                }
                this.setViewEnable(this.ivUndo, false);
                this.setViewEnable(this.ivCancel, true);
                break;
        }
        return true;
    }
}
