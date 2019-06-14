package com.example.notepad.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import com.example.notepad.Helper.Util;
import com.example.notepad.R;

public class ClearEditText extends AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {
    //删除按钮的引用
    private Drawable mClearDrawable;

    //控件是否有焦点
    private boolean hasFocus;

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //获取EditText的DrawableRight,假如没有设置使用默认的图片
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
            mClearDrawable = getResources().getDrawable(R.mipmap.cancel);
        }
        //设置图片大小
        mClearDrawable.setBounds(0, 0,
                Util.parseXMLToPX(R.dimen.size_mini_icon, getContext()),
                Util.parseXMLToPX(R.dimen.size_mini_icon, getContext()));
        //默认隐藏图标
        setClearIconVisible(false);
        //设置焦点改变的监听
        setOnFocusChangeListener(this);
        //设置内容监听
        addTextChangedListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mClearDrawable != null && event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            //判断触摸点是否在水平范围内
            boolean isInnerWidth = (x > (getWidth() - getTotalPaddingRight())) &&
                    (x < (getWidth() - getPaddingRight()));
            //获取删除图标的边界，返回一个Rect对象
            Rect rect = mClearDrawable.getBounds();
            //获取删除图标的高度
            int height = rect.height();
            int y = (int) event.getY();
            //计算图标底部到控件底部的距离
            int distance = (getHeight() - height) / 2;
            //判断触摸点是否在竖直范围内
            boolean isInnerHeight = (y > distance) && (y < (distance + height));
            if (isInnerHeight && isInnerWidth) {
                this.setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    //设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
    private void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1],
                right, getCompoundDrawables()[3]);
    }

    //焦点变化
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFocus = hasFocus;
        if (hasFocus) {
            setClearIconVisible(getText() != null && getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    //内容变化
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (hasFocus) {
            setClearIconVisible(text.length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    //设置晃动动画
    public void setShakeAnimation() {
        this.startAnimation(shakeAnimation(5));
    }

    //晃动动画
    public static Animation shakeAnimation(int counts) {
        //初始化动画
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        //设置循环次数
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        //设置持续时长 1s
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }
}
