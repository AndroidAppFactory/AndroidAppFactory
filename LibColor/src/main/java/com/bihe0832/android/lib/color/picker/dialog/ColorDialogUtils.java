/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 上午10:45
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/25 上午10:45
 *
 */

package com.bihe0832.android.lib.color.picker.dialog;

import android.content.Context;
import android.graphics.Color;
import com.bihe0832.android.lib.color.utils.ColorUtils;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedIntCallback;
import com.bihe0832.android.lib.ui.dialog.callback.DialogIntCallback;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.utils.os.DisplayUtil;
import com.ricky.color_picker.R;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2022/5/25.
 *         Description: Description
 */
public class ColorDialogUtils {

    public static final int DEFAULT_PICK_DP = 160;

    public static void showColorSelectDialog(final Context context, int pickType, int defaultAlpha, int defaultValue,
            float widthDp, String positive, String negative, String tips, Boolean canCanceledOnTouchOutside,
            final DialogIntCallback listener) {
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                final ColorPickDialog dialog = new ColorPickDialog(context, pickType);
                dialog.setCurrentColorAlpha(defaultAlpha);
                dialog.setCurrentColorText(defaultValue);
                dialog.setWidth(widthDp);
                dialog.setPositive(positive);
                dialog.setNegative(negative);
                dialog.setFeedBackContent(tips);
                dialog.setShouldCanceled(canCanceledOnTouchOutside);
                dialog.setOnClickBottomListener(new OnDialogListener() {
                    @Override
                    public void onPositiveClick() {
                        try {
                            listener.onPositiveClick(dialog.getCurrentColorText());
                            dialog.dismiss();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNegativeClick() {
                        try {
                            dialog.reset();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancel() {
                        try {
                            listener.onCancel(dialog.getDefaultColor());
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    public static void showColorSelectDialog(final Context context, int pickType, int defaultAlpha, int defaultValue,
            final DialogIntCallback listener) {
        showColorSelectDialog(context, pickType, defaultAlpha, defaultValue,
                DisplayUtil.dip2px(context, DEFAULT_PICK_DP),
                ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.lib.aaf.res.R.string.dialog_color_button_ok),
                ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.lib.aaf.res.R.string.dialog_color_button_reset),
                ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.lib.aaf.res.R.string.dialog_color_tips), true, listener);
    }

    public static void showColorSelectDialog(final Context context, int pickType, int defaultAlpha, int defaultValue,
            final DialogCompletedIntCallback listener) {
        showColorSelectDialog(context, pickType, defaultAlpha, defaultValue, new DialogIntCallback() {
            @Override
            public void onPositiveClick(int result) {
                listener.onResult(result);
            }

            @Override
            public void onNegativeClick(int result) {
                listener.onResult(result);
            }

            @Override
            public void onCancel(int result) {
                listener.onResult(result);
            }
        });
    }

    public static void showColorSelectDialog(final Context context, int defaultAlpha, int defaultValue,
            final DialogCompletedIntCallback listener) {
        showColorSelectDialog(context, ColorPickDialog.TYPE_WHELL, defaultAlpha, defaultValue, listener);
    }

    public static void showColorSelectDialog(final Context context, String defaultValue,
            final DialogIntCallback listener) {
        int color = Color.parseColor(defaultValue);
        showColorSelectDialog(context, ColorPickDialog.TYPE_WHELL, ColorUtils.getAlpha(color),
                ColorUtils.removeAlpha(color), listener);
    }

    public static void showColorSelectDialog(final Context context, String defaultValue,
            final DialogCompletedIntCallback listener) {
        int color = Color.parseColor(defaultValue);
        showColorSelectDialog(context, ColorPickDialog.TYPE_WHELL, ColorUtils.getAlpha(color),
                ColorUtils.removeAlpha(color), listener);
    }
}
