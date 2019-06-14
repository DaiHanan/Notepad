package com.example.notepad.Controller;

import android.content.Context;
import android.util.Log;

import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.Util;
import com.example.notepad.Model.Note;
import com.example.notepad.Model.Responce;
import com.example.notepad.Model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//post传递请求线程
public class HttpThread implements Runnable {

    //路由
    private String url;
    //请求的数据
    private HashMap<String, String> request;
    //响应的数据
    private Responce responce;

    //是否结束
    private static boolean isFinished;

    /**
     * 开启post请求
     *
     * @param url      请求路由
     * @param request  请求对应键值对
     * @param responce 响应
     * @param context  请求页面
     */
    public static void startHttpThread(String url, HashMap<String, String> request,
                                       Responce responce, Context context) {
        HttpThread.isFinished = false;
        //开启加载框并自动消失
        Util.startLoad(context, "");
        new Thread(new LoadThread()).start();
        //post线程
        Thread thread = new Thread(new HttpThread(url, request, responce));
        try {
            //开启线程
            thread.start();
            //主线程等待
            thread.join(Config.maxThreadTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isFinished = true;
        //查看是否请求成功
        if (!responce.flag) {//请求失败
            Util.sendToast(context, "网络请求失败");
        } else if (!responce.msg.equals("")) {//出错了
            Util.sendToast(context, responce.msg);
        }
    }

    /**
     * 初始化post传递请求线程
     *
     * @param url      对应的路由
     * @param request  请求的数据集
     * @param responce 响应的数据集
     */
    private HttpThread(String url, HashMap<String, String> request, Responce responce) {
        this.url = url;
        this.request = request;
        this.responce = responce;
    }

    @Override
    public void run() {
        //发送post请求
        String responce = HttpUtil.submitPostData(Config.IP + this.url, this.request);
        if (responce.equals("-1") || responce.equals("-2")) return;
        //分析JSON数据并设置flag（是否响应成功）
        this.responce.flag = this.parseJSON(responce);
    }

    /**
     * @param jsonStr 要转换的JSON
     *                格式：{
     *                code:0,
     *                data:{
     *                id:3,
     *                list:[
     *                {id:3},
     *                ]
     *                }
     *                }
     * @return 是否转换成功
     */
    private boolean parseJSON(String jsonStr) {
        try {
            Responce responce = this.responce;
            //主体
            JSONObject main = new JSONObject(jsonStr);
            //获得code
            responce.code = main.getInt("code");

            if (responce.code != 0) {//如果没有成功读取msg
                responce.msg = main.getString("msg");
            } else {//成功了则读取数据
                JSONObject data;
                switch (this.url) {
                    case Config.URL_LOGIN://登录
                        //获得data对象
                        data = main.getJSONObject("data");
                        //读取信息
                        responce.user = new User(
                                data.getInt("id"),
                                data.getString("name"),
                                data.getString("email")
                        );
                        break;
                    case Config.URL_NOTE_LIST://便签列表
                        //获得data对象
                        data = main.getJSONObject("data");
                        //读取信息
                        JSONArray list = data.getJSONArray("list");
                        //循环读取数组信息
                        for (int i = 0; i < list.length(); i++) {
                            //获得数组元素
                            JSONObject note = list.getJSONObject(i);
                            //读取
                            responce.notes.add(new Note(
                                    note.getInt("id"),
                                    note.getString("text"),
                                    note.getLong("create_time"),
                                    note.getLong("modify_time"),
                                    note.getInt("top")
                            ));
                        }
                        break;
                    case Config.URL_NOTE_DETAIL://详情
                        //获得data对象
                        data = main.getJSONObject("data");
                        //读取信息
                        responce.notes.add(new Note(data.getString("text")));
                        break;
                }
            }

        } catch (JSONException e) {
            Log.d("jsonErr", e.getMessage());
            return false;
        }

        return true;
    }

    //子线程，用于关闭加载框
    static class LoadThread implements Runnable {
        @Override
        public void run() {
            try {
                while (!HttpThread.isFinished) {
                    Thread.sleep(20);
                }
                Util.stopLoad();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class HttpUtil {

    /**
     * Function  :   发送Post请求到服务器
     * Param     :   params请求体内容，encode编码格式
     */
    static String submitPostData(String strUrlPath, Map<String, String> params) {

        byte[] data = getRequestData(params).toString().getBytes();//获得请求体
        try {

            URL url = new URL(strUrlPath);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream);                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            //e.printStackTrace();
            Log.d("e", "err: " + e.getMessage());
            return "-2";
        }
        return "-1";
    }

    /**
     * Function  :   封装请求体信息
     * Param     :   params请求体内容，encode编码格式
     */
    private static StringBuffer getRequestData(Map<String, String> params) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), Config.encodeStr))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    /**
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     */
    private static String dealResponseResult(InputStream inputStream) {
        String resultData;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        try {
            int len;
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }

}