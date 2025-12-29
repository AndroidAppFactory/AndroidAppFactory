package com.bihe0832.android.lib.ui.menu;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.bihe0832.android.lib.ui.dialog.R;
import com.bihe0832.android.lib.ui.view.ext.ViewExtKt;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

public class PopMenu {

    // 更多menu
    private ListView mMenuList;
    private PopMenuAdapter mMenuAdapter;
    private PopupWindow mMenuWindow;
    private List<PopMenuItem> mActions = new ArrayList<>();
    protected Activity mActivity;
    private View mAttachView;
    private int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
    private Drawable mBackgroundDrawable = null;
    private int gravity = Gravity.RIGHT;
    //横向偏移量（向与graviry对齐的方向移动移可用负数，例如gravity 为right，左移用负数）
    private int xOffset = 0;
    //下移偏移量（上移可用负数）
    private int yOffset = 0;
    private float bgAlpha = 1.0f;

    public PopMenu(Activity activity, View attach) {
        mActivity = activity;
        mAttachView = attach;
        mWidth = (int) mActivity.getResources().getDimension(com.bihe0832.android.lib.aaf.res.R.dimen.com_bihe0832_pop_menu_width);
        mBackgroundDrawable = mActivity.getResources().getDrawable(com.bihe0832.android.lib.aaf.res.R.drawable.com_bihe0832_base_pop_menu_bg);
        xOffset = DisplayUtil.dip2px(activity, -8);
        yOffset = DisplayUtil.dip2px(activity, 8);
    }

    public void setMenuItemList(List<PopMenuItem> menuActions) {
        mActions.clear();
        mActions.addAll(menuActions);
    }

    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        this.mBackgroundDrawable = backgroundDrawable;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }


    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public void setBgAlpha(float bgAlpha) {
        this.bgAlpha = bgAlpha;
    }

    public void setOffset(int xOffset, int yOffset) {
        setXOffset(xOffset);
        setYOffset(yOffset);
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public boolean isShowing() {
        if (mMenuWindow == null) {
            return false;
        }
        return mMenuWindow.isShowing();
    }

    public void hide() {
        mMenuWindow.dismiss();
    }

    public void show() {
        if (mActions == null || mActions.size() == 0) {
            return;
        }
        mMenuWindow = new PopupWindow(mActivity);
        mMenuAdapter = new PopMenuAdapter(mActivity);

        mMenuAdapter.setDataSource(mActions);
        View menuView = LayoutInflater.from(mAttachView.getContext()).inflate(R.layout.com_bihe0832_pop_menu_layout, null);
        // 设置布局文件
        mMenuWindow.setContentView(menuView);

        mMenuList = menuView.findViewById(R.id.conversation_pop_list);
        mMenuList.setAdapter(mMenuAdapter);
        mMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PopMenuItem action = (PopMenuItem) mMenuAdapter.getItem(position);
                if (action != null && action.getActionClickListener() != null) {
                    action.getActionClickListener().onClick(view);
                }
            }
        });
        mMenuWindow.setWidth(mWidth);
        mMenuWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mMenuWindow.setBackgroundDrawable(mBackgroundDrawable);
        // 设置pop获取焦点，如果为false点击返回按钮会退出当前Activity，如果pop中有Editor的话，focusable必须要为true
        mMenuWindow.setFocusable(true);
        // 设置pop可点击，为false点击事件无效，默认为true
        mMenuWindow.setTouchable(true);
        // 设置点击pop外侧消失，默认为false；在focusable为true时点击外侧始终消失
        mMenuWindow.setOutsideTouchable(true);
        ViewExtKt.changeBackgroundAlpha(mActivity, bgAlpha);
        // 相对于 + 号正下面，同时可以设置偏移量
        mMenuWindow.showAsDropDown(mAttachView, xOffset, yOffset, gravity);
        // 设置pop关闭监听，用于改变背景透明度
        mMenuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ViewExtKt.changeBackgroundAlpha(mActivity, 1.0f);
            }
        });
    }

}
