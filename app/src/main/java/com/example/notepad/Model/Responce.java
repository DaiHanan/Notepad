package com.example.notepad.Model;

import java.util.ArrayList;

//http响应数据
public class Responce {
    //数据是否接收成功
    public boolean flag;
    //响应code
    public int code;
    //响应msg
    public String msg;

    //用户信息
    public User user;
    //便签信息
    public ArrayList<Note> notes;

    public Responce() {
        code = -1;
        msg = "";
        flag = false;
        notes = new ArrayList<>();
    }
}
