package com.example.notepad.Controller;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;

//页面栈，用于管理页面并提供退出程序方法
public class ActivityContainer {

    private ActivityContainer() {
    }

    //结束所有Activity
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

//    //注销用 重新进入应用
//    public void restartApp(Activity activity) {
//        this.finishAllActivity();
//        Intent intent = activity.getBaseContext().
//                getPackageManager().
//                getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
//        if(intent != null) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            activity.startActivity(intent);
//            for(int i = 0; i < activityStack.size() - 1; i++){
//                activityStack.remove(i);
//            }
//        }
//    }

    //单例
    private static ActivityContainer instance = new ActivityContainer();
    //管理页面的栈结构
    private static List<Activity> activityStack = new ArrayList<>();

    //获得单例
    public static ActivityContainer getInstance() {
        return instance;
    }
    //添加页面
    public void addActivity(Activity aty) {
        activityStack.add(aty);
    }
    //移出页面
    public void removeActivity(Activity aty) {
        activityStack.remove(aty);
    }

}