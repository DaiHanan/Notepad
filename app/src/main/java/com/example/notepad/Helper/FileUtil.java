package com.example.notepad.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileUtil {
    //文件实例
    private static SharedPreferences shared;
    //离线文件实例
    private static SharedPreferences tmpShared;

    //初始化实例
    public static void setInstance(SharedPreferences shared, SharedPreferences tmpShared) {
        FileUtil.shared = shared;
        FileUtil.tmpShared = tmpShared;
    }

    //存储 单
    public static void save(String key, String value) {
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(key, value);
        editor.apply();
    }

    //存储 多
    public static void save(HashMap<String, String> map) {
        SharedPreferences.Editor editor = shared.edit();
        //将数组每个键值对存储起来
        for (Map.Entry<String, String> entry : map.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();
    }

    //读取
    public static String read(String key) {
        return shared.getString(key, "");
    }

    //读取(有默认值
    public static String read(String key, String def) {
        return shared.getString(key, def);
    }

    //读取 读取失败时提示用户
    public static String read(String key, boolean force, Context context) {
        String str = shared.getString(key, Config.FORCE_DEFAULT);
        //读取失败时
        if(force && (str == null || str.equals(Config.FORCE_DEFAULT))) {
            Util.sendAlert(context, Config.FORCE_ALERT);
        }
        return str;
    }

    //离线文件读写
    //读
    public static ArrayList<String> readTmp() {
        //读取数据并且返回数组
        String value = tmpShared.getString(Config.OFFLINE_KEY, "");
        return value == null || value.isEmpty()
                ? new ArrayList<String>()
                : new ArrayList<>(Arrays.asList(value.split(Config.OFFLINE_NOTE_BREAK)));
    }
    //写
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void addTmp(int id, String value) {
        //读取数据
        ArrayList<String> array = FileUtil.readTmp();
        //新增数据
        array.add("" + id + Config.OFFLINE_NOTE_ID_BREAK + value);
        //添加
        SharedPreferences.Editor editor = tmpShared.edit();
        editor.putString(Config.OFFLINE_KEY, String.join(Config.OFFLINE_NOTE_BREAK, array));
        editor.apply();
    }
    //写（再次上传失败后）
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setTmp(ArrayList<String> array) {
        SharedPreferences.Editor editor = FileUtil.tmpShared.edit();
        editor.putString(Config.OFFLINE_KEY, array.size() != 0
                ? String.join(Config.OFFLINE_NOTE_BREAK, array)
                : "");
        editor.apply();
    }

    //注销用 删除所有当前用户信息
    public static void clear() {
        //信息文件
        SharedPreferences.Editor editor = FileUtil.shared.edit();
        editor.clear();
        editor.apply();
        //临时便签文件
        editor = FileUtil.tmpShared.edit();
        editor.clear();
        editor.apply();
    }
}
