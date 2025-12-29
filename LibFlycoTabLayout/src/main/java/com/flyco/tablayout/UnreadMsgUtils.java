package com.flyco.tablayout;


import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 未读消息提示View,显示小红点或者带有数字的红点:
 * 数字一位,圆
 * 数字两位,圆角矩形,圆角是高度的一半
 * 数字超过两位,显示99+
 */
class UnreadMsgUtils {
    public static void show(TextView msgView, int num) {
        if (msgView == null) {
            return;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) msgView.getLayoutParams();
        msgView.setVisibility(View.VISIBLE);
        if (num <= 0) {//圆点,设置默认宽高
            msgView.setText("");
            int redDotSize = (int) msgView.getContext().getResources().getDimension(com.bihe0832.android.lib.aaf.res.R.dimen.com_bihe0832_tab_red_dot_size);
            lp.width = redDotSize;
            lp.height = redDotSize;
            msgView.setLayoutParams(lp);
        } else {
            int height = (int) msgView.getContext().getResources().getDimension(com.bihe0832.android.lib.aaf.res.R.dimen.com_bihe0832_tab_red_msg_height);
            int padding = (int) msgView.getContext().getResources().getDimension(com.bihe0832.android.lib.aaf.res.R.dimen.com_bihe0832_tab_red_msg_padding);


            lp.height = height;
            if (num > 0 && num < 10) {//圆
                lp.width = height;
                msgView.setText(num + "");
            } else if (num > 9 && num < 100) {//圆角矩形,圆角是高度的一半,设置默认padding
                lp.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                msgView.setPadding(padding, 0, padding, 0);
                msgView.setText(num + "");
            } else {//数字超过两位,显示99+
                lp.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                msgView.setPadding(padding, 0, padding, 0);
                msgView.setText("99+");
            }
            msgView.setLayoutParams(lp);
        }
    }

    public static void show(TextView msgView, String text) {
        if (msgView == null) {
            return;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) msgView.getLayoutParams();
        msgView.setVisibility(View.VISIBLE);
        int height = (int) msgView.getContext().getResources().getDimension(com.bihe0832.android.lib.aaf.res.R.dimen.com_bihe0832_tab_red_msg_height);
        int padding = (int) msgView.getContext().getResources().getDimension(com.bihe0832.android.lib.aaf.res.R.dimen.com_bihe0832_tab_red_msg_padding);

        lp.height = height;
        lp.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        msgView.setPadding(padding, 0, padding, 0);
        msgView.setText(text);
        msgView.setLayoutParams(lp);
    }
}
