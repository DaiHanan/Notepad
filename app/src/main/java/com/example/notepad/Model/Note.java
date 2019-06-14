package com.example.notepad.Model;

import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.FileUtil;
import com.example.notepad.Helper.Util;

import java.util.Date;

//便签信息,支持排序
public class Note implements Comparable<Note> {
    //主键id
    public int id = -1;
    //文本
    public String text;
    //创建时间
    private Date createTime = null;
    //修改时间
    private Date modifyTime = null;
    //是否置顶
    public int top = -1;

    //http响应用 新增
    public Note(String text) {
        this.text = text;
    }

    //http响应用 列表
    public Note(int id, String text, long createTime, long modifyTime, int top) {
        this.id = id;
        this.text = text;
        this.createTime = new Date(createTime);
        this.modifyTime = new Date(modifyTime);
        this.top = top;
    }

    //列表用
    //获得标题-主界面
    public String getTitle() {
        //回车格式 "\n"
        //根据当前布局获得标题

        //先找到开始下标
        int startIndex = 0;
        while (startIndex < this.text.length() && this.text.charAt(startIndex) == '\n')
            ++startIndex;
        //获得标题结束下标
        int titleEndIndex = this.getTitleEndIndex();

        return titleEndIndex != -1 ? //文本全为回车，返回空字符串
                this.text.substring(startIndex, titleEndIndex + 1) : "";
    }

    //获得内容-主界面
    public String getContent() {
        //通过标题结束下标和计算获得内容开始下标
        int titleEndLength = this.getTitleEndIndex();
        int startIndex = titleEndLength + 1;
        while (startIndex < this.text.length() && this.text.charAt(startIndex) == '\n')
            ++startIndex;//去掉开头回车

        if (titleEndLength == -1 || startIndex == this.text.length())
            return "";

        int endIndex = this.text.length() - 1;
        while (endIndex > startIndex && this.text.charAt(endIndex) == '\n')
            --endIndex;//去除结尾的回车

        return this.text.substring(startIndex, endIndex + 1);
    }

    //找到标题结束下标 -1表示没有内容
    private int getTitleEndIndex() {
        //先找到开始下标
        int startIndex = 0;
        while (startIndex < this.text.length() && this.text.charAt(startIndex) == '\n')
            ++startIndex;
        if (startIndex == this.text.length())//文本全为回车，返回空字符串
            return -1;
        //获得标题最大长度
        int maxLength = 0;
        switch (Integer.parseInt(FileUtil.read(Config.NOW_LAYOUT, Config.DEFAULT_LAYOUT + ""))) {
            case Config.LINEAR_LAYOUT:
                maxLength = Config.TITLE_LENGTH_LINEAR;
                break;
            case Config.STAGGERED_LAYOUT:
                maxLength = Config.TITLE_LENGTH_STAGGERED;
                break;
        }
        //计算标题长度
        int length = Math.min(
                maxLength,//长度限制
                this.text.indexOf("\n", startIndex) - startIndex < 0
                        ? this.text.length() //总长度
                        : Math.min(
                                this.text.indexOf("\n", startIndex) - startIndex,
                                this.text.length()));//到下一个回车的长度

        return startIndex + length - 1;
    }

    //获得时间字符串
    public String getTime(int orderType) {
        try {
            return Util.parseDatetoString(orderType == Config.INDEX_ORDER_CREATE
                    ? this.createTime : this.modifyTime);
        } catch (NullPointerException e) {//空指针时
            return "";
        }
    }

    //都为降序
    @Override
    public int compareTo(Note note) {
        //优先置顶
        if (this.top != note.top)
            return note.top - this.top;
        //如果按创建时间排序
        if (FileUtil.read(Config.NOW_ORDER_TYPE, Config.DEFAULT_ORDER_TYPE_INDEX + "")
                .equals(Config.INDEX_ORDER_CREATE + "")) {
            return (int) (note.createTime.getTime() - this.createTime.getTime());
        }
        //按编辑时间排序
        return (int) (note.modifyTime.getTime() - this.modifyTime.getTime());
    }
}
