package com.example.notepad.Helper;

import android.graphics.Color;

import com.example.notepad.R;

public class Config {
    //服务器ip地址
    public static final String IP = "http://47.102.149.16:7001/";
    //url常量
    //用户
    //注册路由 name,email,password
    public static final String URL_REGISTER = "register";
    //登录路由 email,password id(user),name,email
    public static final String URL_LOGIN = "login";
    //修改信息路由 id(user),~name,~email,~password
    public static final String URL_USER_MODIFY = "user/modify";
    //重置密码——发送邮箱 email
    public static final String URL_SEND_EMAIL = "user/reset_send";
    //记事本
    //新添或修改 text,userId,~id(note)
    public static final String URL_NOTE_MODIFY = "note/modify";
    //删除 id(note),userId
    public static final String URL_NOTE_DEL = "note/del";
    //列表 id(user),keyword list:[id,text(20),create_time,modify_time,top]
    public static final String URL_NOTE_LIST = "note/list";
    //详情 userId,id(note) text
    public static final String URL_NOTE_DETAIL = "note/detail";
    //置顶 top,userId,id(note)
    public static final String URL_NOTE_TOP = "note/top";

    //数据传递编码
    public static final String encodeStr = "UTF-8";
    //请求最长等待时间
    public static final int maxThreadTime = 1000 * 5;//5秒

    //请求Code
    //登录请求
    public static final int CODE_LOGIN = 0;
    //注册请求
    public static final int CODE_REGISTER = 1;
    //忘记密码请求
    public static final int CODE_FORGET_EMAIL = 2;
//    //设置请求
//    public static final int CODE_SETTING = 3;
//    //注销请求
//    public static final int CODE_LOGOFF = 4;
//    //便签请求
//    public static final int CODE_NOTE = 5;

    //请求成功标志
    public static final String FLAG = "flag";

    /**
     * 文件存储
     * login 是否登录 0-无用户 1-有用户
     * id 用户id
     * name 用户姓名
     * email 用户邮箱
     * <p>
     * now_font_size 当前文字大小
     * now_order_type 当前排序方式
     * now_quick_del 当前是否快捷删除 0-否、1-是
     * now_layout 当前排序
     */
    //存储文件名
    public static final String FILE_NAME = "data";
    //存储名常量
    //是否登录
    public static final String LOGIN = "login";
    public static final String YES = "1";
    public static final String NO = "0";
    //用户id
    public static final String ID = "id";//请求时有时为便签id
    public static final String USER_ID = "userId";//请求用
    //是否置顶
    public static final String TOP = "top";//请求用
    //关键字
    public static final String KEYWORD = "keyword";//请求用
    //便签内容
    public static final String TEXT = "text";//请求用
    //时间
    public static final String TIME = "time";//传值用
    //用户姓名
    public static final String NAME = "name";
    //用户邮箱
    public static final String EMAIL = "email";
    //用户密码
    public static final String PASSWORD = "password";//请求用
    //当前文字大小
    public static final String NOW_FONT_SIZE = "now_font_size";
    //当前排序方式
    public static final String NOW_ORDER_TYPE = "now_order_type";
    //当前是否快捷删除
    public static final String NOW_QUICK_DEL = "now_quick_del";
    //当前布局
    public static final String NOW_LAYOUT = "now_layout";

    //文字大小常量
    public static final String[] ARR_FONT_SIZE_NAME = new String[]{
            Util.getStringFromXML(R.string.font_size_small, null),
            Util.getStringFromXML(R.string.font_size_normal, null),
            Util.getStringFromXML(R.string.font_size_big, null),
            Util.getStringFromXML(R.string.font_size_super, null),
    };
    public static final int[] ARR_FONT_SIZE = new int[]{//单位sp，需同步修改
//            Util.parseXMLToPX(R.dimen.font_text_small, null),
//            Util.parseXMLToPX(R.dimen.font_text_normal, null),
//            Util.parseXMLToPX(R.dimen.font_text_big, null),
//            Util.parseXMLToPX(R.dimen.font_text_super, null),
            13, 16, 19, 25
    };
    //默认字体大小下标
    public static final int DEFAULT_FONT_SIZE_INDEX = 1;
    //排序方式常量
    public static final String[] ARR_ORDER_TYPE = new String[]{
            Util.getStringFromXML(R.string.order_type_create, null),
            Util.getStringFromXML(R.string.order_type_modify, null),
    };
    //创建日期下标
    public static final int INDEX_ORDER_CREATE = 0;
//    //编辑日期下标
//    public static final int INDEX_ORDER_MODIFY = 1;
    //默认排序方式下标
    public static final int DEFAULT_ORDER_TYPE_INDEX = 1;

    //布局
    public static final int LINEAR_LAYOUT = 0;//线性

    public static final int STAGGERED_LAYOUT = 1;//瀑布
    public static final int STAGGERED_LAYOUT_COLS = 2;//瀑布布局的列数
    //默认布局
    public static final int DEFAULT_LAYOUT = 0;
    //布局名称
    public static final String[] LAYOUT_NAME = new String[]{
            "列表模式",
            "宫格模式"
    };

    //颜色
    public static int colorWhite = Color.WHITE;
    public static int colorPrimary = Color.rgb(226, 226, 226);
    public static int colorAccent = Color.rgb(31,231,231);
    public static int colorPress = Color.rgb(19,172,172);

    //延迟响应时间
    public static int RELAY_TIME = 1500;

    //便签对象
    //时间字符串格式
    static String DATA_FORM = "yyyy年MM月dd日 HH:mm";
    //列表模式下标题长度
    public static int TITLE_LENGTH_LINEAR = 15;
    //宫格模式下标题长度
    public static int TITLE_LENGTH_STAGGERED = 8;
    //宫格模式下校正界面延时
    public static int STAGGERED_MODIFY_INTERVAL = 580;

    //大间隔dp
    public static int BIG_INTERVAL = 10;

    //搜索栏Handler用
    public static int FLAG_SEARCH = 1;//延时搜索标志
    public static int SEARCH_INTERVAL = 300;//300毫秒延时搜索

    //编辑用
    //软键盘弹出延迟
    public static int SOFT_INPUT_INTERVAL = SEARCH_INTERVAL;//300毫秒
    //撤销重做总次数
    public static int CANCEL_UNDO_COUNT = 30;

    //离线存储用
    //文件名
    public static String FILE_NAME_OFFLINE = "tmp_data";
    //数据存储字段名
    static String OFFLINE_KEY = "data";
    //便签分隔符
    static String OFFLINE_NOTE_BREAK = "~！￥￥！￥！";
    //便签id分隔符
    public static String OFFLINE_NOTE_ID_BREAK = "！！￥￥！￥";
    //便签组成部分
    public static int OFFLINE_NOTE_MAKEUP_COUNT = 2;
    //上传成功提示延时
    public static int UPLOAD_SUCCESS_INTERVAL = 500;//500毫秒

    //介绍用
    //自拍弹窗宽度
    public static int AUTHOR_ICON_WIDTH = Util.parseXMLToPX(R.dimen.author_icon_width, null);

    //读取必需数据出错时的获得值
    static String FORCE_DEFAULT = "！c错料by甙寒！";
    //读取必需数据出错时的获得值
    static String FORCE_ALERT = "疑似第三方修改app数据，建议重新登录或清除数据><";

}
