package com.example.notepad.Model;

//用户信息
public class User {
    //主键id
    public int id;
    //姓名
    public String name;
    //邮箱
    public String email;

    //http响应用
    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

}
