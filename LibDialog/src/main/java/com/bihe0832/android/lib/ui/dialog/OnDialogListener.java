package com.bihe0832.android.lib.ui.dialog;

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/5/25.
 * Description: Description
 */
public interface OnDialogListener {
    /**
     * 点击确定按钮事件
     */
    void onPositiveClick();
    /**
     * 点击取消按钮事件
     */
    void onNegtiveClick();
    /**
     * 点击关闭按钮事件
     */
    void onCloseClick();
}
