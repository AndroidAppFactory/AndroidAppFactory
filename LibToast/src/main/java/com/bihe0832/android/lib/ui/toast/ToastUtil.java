package com.bihe0832.android.lib.ui.toast;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.thread.ThreadManager;


public class ToastUtil {

    public static void showLong(final Context ctx, final String toastInfo) {
        show(ctx, toastInfo, Toast.LENGTH_LONG);
    }

    public static void showShort(final Context ctx, final String toastInfo) {
        show(ctx, toastInfo, Toast.LENGTH_SHORT);
    }

    public static void showTop(final Context ctx, final String toastInfo, final int duration) {
        show(ctx, R.mipmap.icon, toastInfo, ctx.getResources().getDimension(R.dimen.bihe0832_common_toast_text_size),
                duration, Gravity.TOP, 0,
                (int) ctx.getResources().getDimension(R.dimen.bihe0832_common_toast_y_offset));
    }

    public static void show(final Context ctx, final String toastInfo, final int duration) {
        show(ctx, R.mipmap.icon, toastInfo, ctx.getResources().getDimension(R.dimen.bihe0832_common_toast_text_size),
                duration, Gravity.BOTTOM, 0,
                (int) ctx.getResources().getDimension(R.dimen.bihe0832_common_toast_y_offset));
    }

    public static void showLongTips(final Context ctx, int res, final String toastInfo) {
        showTips(ctx, res, toastInfo, Toast.LENGTH_LONG);
    }

    public static void showShortTips(final Context ctx, int res, final String toastInfo) {
        showTips(ctx, res, toastInfo, Toast.LENGTH_SHORT);
    }

    public static void showTips(final Context ctx, int res, final String toastInfo, final int duration) {
        showTips(ctx, res, toastInfo, ctx.getResources().getDimension(R.dimen.bihe0832_common_toast_text_size),
                duration, Gravity.CENTER, 0, 0);
    }

    public static void show(final Context ctx, final int imageRes, final String toastInfo, final float textSize,
            final int duration, final int gravityType, final int xOffset, final int yOffset) {
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                View contentView = LayoutInflater.from(ctx).inflate(R.layout.com_bihe0832_base_toast, null);
                final View layout = contentView.findViewById(R.id.bihe0832_common_custom_toast_layout_id);
                ImageView toastImage = (ImageView) layout.findViewById(R.id.bihe0832_common_toast_image);
                toastImage.setImageResource(imageRes);
                show(ctx, layout, toastInfo, textSize, duration, gravityType, xOffset, yOffset);
            }
        });
    }

    public static void showTips(final Context ctx, final int imageRes, final String toastInfo, final float textSize,
            final int duration, final int gravityType, final int xOffset, final int yOffset) {
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                View contentView = LayoutInflater.from(ctx).inflate(R.layout.com_bihe0832_base_toast_tips, null);
                final View layout = contentView.findViewById(R.id.bihe0832_common_custom_toast_layout_id);
                ImageView toastImage = (ImageView) layout.findViewById(R.id.bihe0832_common_toast_image);
                toastImage.setImageResource(imageRes);
                show(ctx, layout, toastInfo, textSize, duration, gravityType, xOffset, yOffset);
            }
        });
    }

    public static void show(final Context ctx, final View layout, final String toastInfo, final float textSize,
            final int duration, final int gravityType, final int xOffset, final int yOffset) {
        TextView toastText = (TextView) layout.findViewById(R.id.bihe0832_common_toast_text);
        CharSequence charSequence = TextFactoryUtils.getSpannedTextByHtml(toastInfo);//支持html
        toastText.setText(charSequence);
        toastText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        show(ctx, layout, duration, gravityType, xOffset, yOffset);

    }

    public static void show(final Context ctx, final View layout, final int duration, final int gravityType,
            final int xOffset, final int yOffset) {
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast toast = new Toast(ctx);
                    toast.setDuration(duration);
                    toast.setView(layout);
                    toast.setGravity(gravityType, xOffset, yOffset);
                    toast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
}  