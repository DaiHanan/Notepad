package com.example.notepad.Helper;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notepad.Controller.ContentApplication;
import com.example.notepad.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    //设置状态栏背景颜色
    public static void setStatusColor(Window window, int color) {
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(color);
    }

    //设置状态栏字体颜色深
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setStatusFontColorDep(Window window) {
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏文字颜色及图标为深色
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );
//        //默认设置颜色为浅灰(主题色)
//        window.setStatusBarColor(Color.rgb(240, 240, 240));
    }

    //Toast单例
    private static Toast toast;

    //弹出Toast提示
    @SuppressLint("ShowToast")
    public static void sendToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, " ", Toast.LENGTH_LONG);
        }
        toast.setText(msg);
        toast.show();
    }

    //弹出AlertDialog提示
    static void sendAlert(final Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(msg).setPositiveButton("了解", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Util.sendToast(context, "嗯唔~");
            }
        }).show();
    }

    //弹出SnackBar提示 默认延时300ms
    public static void sendSnackBar(final View mainView, final String msg) {
        Util.sendSnackBar(mainView, msg, Config.UPLOAD_SUCCESS_INTERVAL);
    }

    //弹出SnackBar提示 设置延时
    private static void sendSnackBar(final View mainView, final String msg, long time) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar.make(mainView, " ", Snackbar.LENGTH_LONG);
                //获取SnackBar的view
                View view = snackbar.getView();
                // 修改view的背景色
                view.setBackgroundColor(Config.colorAccent);
                // 获取SnackBar的message控件，修改字体颜色
                ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(Config.colorWhite);
                //设置显示文本
                snackbar.setText(msg);

                snackbar.show();
            }
        }, time);
    }

    //加载框
    private static AlertDialog load;

    //弹出加载框
    public static void startLoad(Context context, String msg) {
        //上个加载框未结束时不再弹出
        if (load == null) {
            //加载框视图
            @SuppressLint("InflateParams") View loadView = LayoutInflater.from(context).inflate(R.layout.layout_progress, null);
            //设置文本
            if (!msg.equals("")) {
                TextView tv = loadView.findViewById(R.id.text);
                tv.setText(msg);
            }
            load = new AlertDialog.Builder(context).setView(loadView).setCancelable(false).show();
//            //设置宽度
//            final WindowManager.LayoutParams params = load.getWindow().getAttributes();
//            params.width = 800;
//            params.height = 1200;
//            load.getWindow().setAttributes(params);
//            load.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    //取消加载框
    public static void stopLoad() {
        if (load != null) {
            load.dismiss();
            load = null;
        }
    }

    //检查邮箱
    public static boolean checkEmail(String email) {
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    //检查昵称
    public static boolean checkName(String name) {
        return !name.isEmpty();
    }

    //检查密码
    public static boolean checkPassword(String password) {
        return password.length() >= 6 && password.length() <= 50;
    }

    //diments.xml转px
    public static int parseXMLToPX(int id, Context context) {
        //如果没传context则读取应用程序context
        if (context == null) {
            context = ContentApplication.getContext();
        }
        return context.getResources().getDimensionPixelOffset(id);
    }

    //string.xml获得String
    public static String getStringFromXML(int id, Context context) {
        //如果没传context则读取应用程序context
        if (context == null) {
            context = ContentApplication.getContext();
        }
        return context.getString(id);
    }

    //判断手势是否移出某组件
    public static boolean isTouchPointInView(View view, float x, float y) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();

        return y >= top && y <= bottom && x >= left
                && x <= right;
    }

    //时间转时间字符串
    @SuppressLint("SimpleDateFormat")
    public static String parseDatetoString(Date date) {
        return new SimpleDateFormat(Config.DATA_FORM).format(date);
    }

    //判断当前是否有网络
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            //获得app的数据连接管理服务
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            //获得网络信息
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
