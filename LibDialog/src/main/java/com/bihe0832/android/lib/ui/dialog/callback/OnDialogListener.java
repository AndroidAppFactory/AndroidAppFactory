package com.bihe0832.android.lib.ui.dialog.callback;

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
    void onNegativeClick();
    /**
     * 点击其余位置取消，当 shouldCanceledOutside 为 true 时会回调
     */
    void onCancel();
}
