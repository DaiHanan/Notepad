package com.example.notepad.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.example.notepad.Helper.Config;
import com.example.notepad.Helper.FileUtil;
import com.example.notepad.Helper.Util;
import com.example.notepad.Model.Note;
import com.example.notepad.Model.Responce;
import com.example.notepad.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

//便签recycleView适配器
public class NoteAdapter extends RecyclerSwipeAdapter<NoteAdapter.NoteViewHolder> {
    //主视图
    private Context mainContext;
    //数据集合
    private ArrayList<Note> notes;

    public NoteAdapter(Context context, ArrayList<Note> notes) {
        this.mainContext = context;
        this.notes = notes;
    }

    //储存器
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;//整体布局
        LinearLayout foreLayout;//前景布局
        TextView textViewTitle;//标题文本框
        TextView textViewData;//内容文本框
        TextView textViewTime;//时间文本框
        Button buttonDelete;//删除按钮
        Button buttonTop;//置顶按钮

        NoteViewHolder(View itemView) {
            super(itemView);
            swipeLayout = itemView.findViewById(R.id.swipe);
            foreLayout = itemView.findViewById(R.id.fore_view);
            textViewTitle = itemView.findViewById(R.id.text_title);
            textViewData = itemView.findViewById(R.id.text_data);
            textViewTime = itemView.findViewById(R.id.text_time);
            buttonDelete = itemView.findViewById(R.id.delete);
            buttonTop = itemView.findViewById(R.id.top);
        }
    }

    //创建储存器
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //根据列表排序方式选择item布局
        int layout = R.layout.note_item_linear;
        switch (Integer.parseInt(FileUtil.read(Config.NOW_LAYOUT, Config.DEFAULT_LAYOUT + ""))) {
            case Config.LINEAR_LAYOUT:
                layout = R.layout.note_item_linear;
                break;
            case Config.STAGGERED_LAYOUT:
                layout = R.layout.note_item_staggered;
                break;
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new NoteViewHolder(view);
    }

    //绑定储存器
    @Override
    public void onBindViewHolder(@NonNull final NoteViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        //获得当前数据对象
        Note item = notes.get(position);
        //设置模式
        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        //设置删除和星星图标动画
        viewHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.star));
            }
        });
        //设置布局点击监听器
        viewHolder.foreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //接口回调
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });
        //设置删除图标监听器
        viewHolder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //查询设置是否为快捷删除
                if(FileUtil.read(Config.NOW_QUICK_DEL, Config.NO).equals(Config.YES)) {//快捷删除
                    if(sendDelPost(position)) {//服务器删除成功
                        delEffect(position, viewHolder.swipeLayout);
                    }
                } else {//删除确认
                    sendAlertSureDel(position, viewHolder.swipeLayout);
                }
            }
        });
        //设置置顶图标监听器
        viewHolder.buttonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //更新top
                Note note = notes.get(position);
                note.top = Math.abs(note.top - 1);//0、1置换
                //请求更新
                //存放用户id、便签id、是否置顶
                HashMap<String, String> msg = new HashMap<>();
                msg.put(Config.ID, note.id + "");//便签id
                msg.put(Config.USER_ID, FileUtil.read(Config.ID, true, mainContext));//用户id
                msg.put(Config.TOP, note.top + "");//是否置顶
                //创建响应并发送请求 列表
                Responce responce = new Responce();
                HttpThread.startHttpThread(Config.URL_NOTE_TOP, msg, responce, mainContext);
                //处理成功响应
                if (responce.code == 0) {
                    //移动位置
                    //todo 列表闪烁问题没有解决
                    //一、删除
                    notes.remove(note);
                    //二、添加 判断是置顶还是取消置顶
                    if (note.top == 1) {//置顶
                        notes.add(0, note);
                        notifyItemMoved(position, 0);
                    } else {//取消置顶
                        //查找插入位置来显示添加效果
                        notes.add(note);
                        Collections.sort(notes);
                        notifyItemMoved(position, notes.indexOf(note));
                    }
                    //等待动画结束时校正页面
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            notifyDataSetChanged();
                        }
                    }, Config.STAGGERED_MODIFY_INTERVAL);

                    //对范围内的元素进行重新绑定储存器onBindViewHolder
                    notifyItemRangeChanged(0, notes.size());
                    //关闭item的侧滑效果
                    mItemManger.closeAllItems();
                } else {//失败，置回top
                    note.top = Math.abs(note.top - 1);//0、1置换
                }
            }
        });
        //设置显示内容
        viewHolder.textViewTitle.setText(item.getTitle());
        viewHolder.textViewData.setText(item.getContent());
        viewHolder.textViewTime.setText(
                item.getTime(
                        Integer.parseInt(
                                FileUtil.read(
                                        Config.NOW_ORDER_TYPE,
                                        Config.DEFAULT_ORDER_TYPE_INDEX + ""))));
        //根据信息设置置顶按钮的字
        if (notes.get(position).top == 1) //如果已置顶则更换
            viewHolder.buttonTop.setText(Util.getStringFromXML(R.string.unTop, mainContext));
        else {
            viewHolder.buttonTop.setText(Util.getStringFromXML(R.string.top, mainContext));
        }
        //绑定视图后帮助recycleView保持开放?
        mItemManger.bindView(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    //自定义回调接口，用于单击事件
    public interface OnItemClickListener {
        void onClick(int position);
    }

    //接口实例
    private OnItemClickListener listener;

    //设置接口
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    //弹窗-是否删除
    @SuppressLint("ClickableViewAccessibility")
    private void sendAlertSureDel(final int position, final SwipeLayout swipeLayout) {
        //弹出提示框
        final AlertDialog dialog = new AlertDialog.Builder(mainContext).create();
        dialog.show();
        //获得提示框内容
        Window window = dialog.getWindow();
        if (window != null) {
            //设置布局
            window.setContentView(R.layout.layout_delete_sure);
            //绑定
            Button btnCancel = window.findViewById(R.id.cancel);
            Button btnSure = window.findViewById(R.id.sure);
            //设置监听
            btnCancel.setOnTouchListener(new ColorChangeOnTouchListener());
            btnSure.setOnTouchListener(new ColorChangeOnTouchListener());
            btnCancel.setOnClickListener(new View.OnClickListener() {//取消
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            btnSure.setOnClickListener(new View.OnClickListener() {//确定
                @Override
                public void onClick(View v) {
                    //服务器删除成功时
                    if (sendDelPost(position)) {
                        delEffect(position, swipeLayout);
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    //删除请求
    private boolean sendDelPost(int position) {
        //存放用户id，便签id
        HashMap<String, String> msg = new HashMap<>();
        msg.put(Config.USER_ID, FileUtil.read(Config.ID, true, this.mainContext));
        msg.put(Config.ID, notes.get(position).id + "");
        //创建响应并发送请求 列表
        Responce responce = new Responce();
        HttpThread.startHttpThread(Config.URL_NOTE_DEL, msg, responce, mainContext);
        //返回是否成功
        return responce.code == 0;
    }

    //删除效果
    private void delEffect(int position, SwipeLayout swipeLayout) {
        //布局管理器中删除元素
        mItemManger.removeShownLayouts(swipeLayout);
        //数据中删除
        notes.remove(position);
        //显示删除动画
        notifyItemRemoved(position);
        //对范围内的元素进行重新绑定储存器onBindViewHolder
        notifyItemRangeChanged(position, notes.size());
        //关闭item的侧滑效果
        mItemManger.closeAllItems();
    }

}
