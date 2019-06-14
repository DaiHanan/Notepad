package com.example.notepad.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.daimajia.swipe.util.Attributes;
import com.example.notepad.Controller.ColorChangeOnTouchListener;
import com.example.notepad.Controller.HttpThread;
import com.example.notepad.Controller.NoteAdapter;
import com.example.notepad.Controller.NoteStaggeredItemDecoration;
import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.FileUtil;
import com.example.notepad.Helper.Util;
import com.example.notepad.Model.Note;
import com.example.notepad.Model.Responce;
import com.example.notepad.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends DbExitActivity {
    //新建便签按钮
    private FloatingActionButton plus;
    //搜索框
    private ClearEditText etSearch;
    //昵称文本框
    private TextView tvName;
    //便签列表
    private RecyclerView rwNotes;

    //当前模式下标-无需再次读文件
    private int nowLayoutIndex;

//    //当前便签列表
//    private ArrayList<Note> notes;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化存储文件和离线数据文件
        FileUtil.setInstance(
                getSharedPreferences(Config.FILE_NAME, MODE_PRIVATE),
                getSharedPreferences(Config.FILE_NAME_OFFLINE, MODE_PRIVATE));
        //获得当前布局
        nowLayoutIndex = Integer.parseInt(FileUtil.read(Config.NOW_LAYOUT, Config.DEFAULT_LAYOUT + ""));

        //设置toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //取消toolbar自带标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        //绑定
        plus = findViewById(R.id.plus);
        etSearch = findViewById(R.id.search_text);
        tvName = findViewById(R.id.title_name);
        rwNotes = findViewById(R.id.content);
        //添加按钮监听器
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });
        plus.setOnTouchListener(new ColorChangeOnTouchListener());
        //设置搜索框左边图标大小 xml文件必须设置left，不能设置start
        Drawable mSearchDrawable = etSearch.getCompoundDrawables()[0];
        mSearchDrawable.setBounds(0, 0,
                getResources().getDimensionPixelOffset(R.dimen.size_mini_icon),
                getResources().getDimensionPixelOffset(R.dimen.size_mini_icon));
        //刷新图标，否则错位
        etSearch.setCompoundDrawables(
                mSearchDrawable, null, etSearch.getCompoundDrawables()[2], null);
        //设置搜索框文本监听器，延时搜索
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            //文字改变后
            @Override
            public void afterTextChanged(Editable s) {
                //清除之前未开始的操作
                if (searchHandler.hasMessages(Config.FLAG_SEARCH)) {
                    searchHandler.removeMessages(Config.FLAG_SEARCH);
                }
                //延时执行
                searchHandler.sendEmptyMessageDelayed(Config.FLAG_SEARCH, Config.SEARCH_INTERVAL);
            }
        });
    }

    //延时搜索用Handler
    @SuppressLint("HandlerLeak")
    class SearchHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Config.FLAG_SEARCH) {
                flashNotes();
            }
        }
    }

    //Handler实例
    private SearchHandler searchHandler = new SearchHandler();

    //设置便签列表
    private void setNotes(final ArrayList<Note> notes) {
//        //赋值
//        this.notes = notes;
        //当前布局管理器
        this.setNowLayoutManager();
        //设置适配器
        RecyclerView.Adapter noteAdapter = new NoteAdapter(this, notes);
        ((NoteAdapter) noteAdapter).setMode(Attributes.Mode.Single);//设置Mode
        ((NoteAdapter) noteAdapter).setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            //设置单击事件回调接口
            @Override
            public void onClick(int position) {//单击后进入便签页面
                Note note = notes.get(position);
                //请求便签详情
                HashMap<String, String> msg = new HashMap<>();//便签id、用户id
                String id = note.id + "";
                msg.put(Config.ID, id);
                msg.put(Config.USER_ID, FileUtil.read(Config.ID, true, MainActivity.this));

                //创建响应并发送请求 列表
                Responce responce = new Responce();
                HttpThread.startHttpThread(Config.URL_NOTE_DETAIL, msg, responce, MainActivity.this);
                //处理成功响应
                if (responce.code == 0) {
                    Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    Bundle bundle = new Bundle();
                    //传递当前便签id、时间字符串和详情
                    bundle.putString(Config.ID, id);
                    bundle.putString(Config.TIME, note.getTime(Integer.parseInt(FileUtil.read(
                            Config.NOW_ORDER_TYPE, Config.DEFAULT_ORDER_TYPE_INDEX + ""
                    ))));
                    bundle.putString(Config.TEXT, responce.notes.get(0).text);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            }
        });
        rwNotes.setAdapter(noteAdapter);

    }

    //获得便签信息
    private ArrayList<Note> getNotes() {
        //存放用户id
        HashMap<String, String> msg = new HashMap<>();//用户id和关键字
        msg.put(Config.ID, FileUtil.read(Config.ID, true, MainActivity.this));
        msg.put(Config.KEYWORD, etSearch.getText() != null ? etSearch.getText().toString() : "");
        //创建响应并发送请求 列表
        Responce responce = new Responce();
        HttpThread.startHttpThread(Config.URL_NOTE_LIST, msg, responce, MainActivity.this);
        //处理成功响应
        if (responce.code == 0) {
            ArrayList<Note> notes = responce.notes;
            //排序
            Collections.sort(notes);
            return notes;
        }
        return null;
    }

    //设置当前布局
    private void setNowLayoutManager() {
        RecyclerView rwNotes = this.rwNotes;
        StaggeredGridLayoutManager layoutManager;
        switch (this.nowLayoutIndex) {
            case Config.LINEAR_LAYOUT://线性
                rwNotes.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                if (rwNotes.getItemDecorationCount() == 0) {
                    rwNotes.addItemDecoration(new RecyclerView.ItemDecoration() {
                        @Override
                        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                            super.getItemOffsets(outRect, view, parent, state);
                            //设置底部padding
                            outRect.bottom = getResources().getDimensionPixelSize(R.dimen.big_interval);
                        }
                    });
                }
                break;
            case Config.STAGGERED_LAYOUT://瀑布
                //关闭自适应item高度
                layoutManager = new StaggeredGridLayoutManager(
                        Config.STAGGERED_LAYOUT_COLS, StaggeredGridLayoutManager.VERTICAL);
                layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
                //设置布局
                rwNotes.setLayoutManager(layoutManager);
                //去除原动画
                rwNotes.setAnimation(null);
                //添加列表装饰器-只添加一次
                if (rwNotes.getItemDecorationCount() == 0) {
                    rwNotes.addItemDecoration(new NoteStaggeredItemDecoration(
                            MainActivity.this,
                            Config.BIG_INTERVAL));
                }

                break;
        }
    }

    //进入页面时
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();

        //检查登录信息,没有登录信息跳转登录界面
        if (!FileUtil.read(Config.LOGIN, Config.NO).equals(Config.YES)) {
            //跳转
            startActivityForResult(
                    new Intent(MainActivity.this, LoginActivity.class),
                    Config.CODE_LOGIN
            );
            return;
        }
        //上传离线便签
        this.uploadOfflineNotes();
        //刷新昵称-可能在设置中被修改
        this.flashName();
        //刷新列表
        this.flashNotes();
    }

    //上传离线便签
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadOfflineNotes() {
        boolean flag = false;//成功标志
        ArrayList<String> array = FileUtil.readTmp();
        if (array.size() > 0) {
            for (int i = 0; i < array.size(); i++) {
                String strNote = array.get(i);
                //第一个元素为id，第二个元素为便签内容
                String[] tmp = strNote.split(Config.OFFLINE_NOTE_ID_BREAK);

                if (tmp.length != Config.OFFLINE_NOTE_MAKEUP_COUNT) {
                    array.remove(strNote);
                } else {
                    //存放userId，id（修改时，即不为-1时）和内容
                    HashMap<String, String> msg = new HashMap<>();
                    msg.put(Config.USER_ID, FileUtil.read(Config.ID, true, MainActivity.this));
                    msg.put(Config.TEXT, tmp[1]);
                    if (Integer.parseInt(tmp[0]) > 0) {
                        msg.put(Config.ID, tmp[0]);
                    }
                    //创建响应并发送请求 新建或修改
                    Responce responce = new Responce();
                    HttpThread.startHttpThread(Config.URL_NOTE_MODIFY, msg, responce, MainActivity.this);
                    //处理成功响应
                    if (responce.code == 0) {
                        array.remove(i--);//从离线便签中删除
                        flag = true;
                    }
                }
            }
            //重新设置离线便签文件信息
            FileUtil.setTmp(array);
            //显示弹窗
            if (flag) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Util.sendSnackBar(
                                MainActivity.this.getWindow().getDecorView(), "离线便签上传成功");
                    }
                }, Config.UPLOAD_SUCCESS_INTERVAL);
            }
        }
    }

    //刷新列表
    private void flashNotes() {
        //设置便签列表
        ArrayList<Note> notes = null;
        if (!Util.isNetworkConnected(MainActivity.this)) {//网络出错
            Util.sendSnackBar(
                    MainActivity.this.getWindow().getDecorView(), "进入离线模式，仅提供添加功能");
        } else {
            //获得列表
            notes = this.getNotes();
            if (notes == null) {//服务器出错
                Util.sendSnackBar(
                        MainActivity.this.getWindow().getDecorView(), "获得列表失败了orz");
            }
        }

        this.setNotes(notes != null ? notes : new ArrayList<Note>());
    }

    //刷新昵称文本框
    private void flashName() {
        //若读取错误则显示app名称
        this.tvName.setText(FileUtil.read(
                Config.NAME, Util.getStringFromXML(
                        R.string.app_name,
                        MainActivity.this)));
    }

    //菜单创建-invalidateOptionsMenu()方法调用后会再次调用此方法
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //菜单选择
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.setting://点击设置图标
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                return true;
            case R.id.model://模式切换
                //更新本地文件
                this.nowLayoutIndex = 1 - this.nowLayoutIndex;
                FileUtil.save(Config.NOW_LAYOUT, this.nowLayoutIndex + "");
                //先删除原布局
                if (this.rwNotes.getItemDecorationCount() != 0) {
                    this.rwNotes.removeItemDecorationAt(0);
                }
                //重新刷新之前先上传离线文件
                this.uploadOfflineNotes();
                //重新刷新列表
                this.flashNotes();
        }

        return super.onOptionsItemSelected(item);
    }

    //菜单每次显示时会调用
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //动态显示模式切换
        menu.findItem(R.id.model).setTitle(Config.LAYOUT_NAME[1 - this.nowLayoutIndex]);

        return super.onPrepareOptionsMenu(menu);
    }

    //返回数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //成功返回时
        if (resultCode == RESULT_OK && data != null) {
            //获得数据
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                switch (requestCode) {
                    case Config.CODE_LOGIN://登录返回
                        //判断是否登录成功
                        if (bundle.getBoolean(Config.FLAG, false)) {
                            //保存用户信息
                            HashMap<String, String> saves = new HashMap<>();
                            saves.put(Config.LOGIN, Config.YES);
                            saves.put(Config.ID, bundle.getInt(Config.ID) + "");
                            saves.put(Config.NAME, bundle.getString(Config.NAME));
                            saves.put(Config.EMAIL, bundle.getString(Config.EMAIL));
                            FileUtil.save(saves);
                            //主界面刷新数据-在onResume中刷新
                        } else {
                            this.finish();
                        }
                        break;
                }
            }

        }
    }

    //返回键
    @Override
    public void onBackPressed() {
        //如果搜索框有内容应该先去除其中内容
        if (this.etSearch.length() > 0) {
            this.etSearch.setText("");
        } else {
            super.onBackPressed();
        }
    }

}
