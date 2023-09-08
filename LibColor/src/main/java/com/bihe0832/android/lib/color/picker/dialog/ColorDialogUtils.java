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
import com.bihe0832.android.lib.color.picker.dialog.impl.ColorPickDialog;
import com.bihe0832.android.lib.color.utils.ColorUtils;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.dialog.OnDialogListener;
import com.ricky.color_picker.R;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2022/5/25.
 *         Description: Description
 */
public class ColorDialogUtils {

    public static void showColorSelectDialog(final Context context, int pickType, int defaultAlpha, int defaultValue,
            String positive, String negative, String tips, Boolean canCanceledOnTouchOutside,
            final ColorDialogCallback listener) {
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                final ColorPickDialog dialog = new ColorPickDialog(context, pickType);
                dialog.setCurrentColorAlpha(defaultAlpha);
                dialog.setCurrentColorText(defaultValue);
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

    public static void showColorSelectDialog(final Context context, int defaultAlpha, int defaultValue,
            final ColorDialogCallback listener) {
        showColorSelectDialog(context,
                ColorPickDialog.TYPE_WHELL,
                defaultAlpha,
                defaultValue,
                ThemeResourcesManager.INSTANCE.getString(R.string.dialog_color_button_ok),
                ThemeResourcesManager.INSTANCE.getString(R.string.dialog_color_button_reset),
                ThemeResourcesManager.INSTANCE.getString(R.string.dialog_color_tips),
                true, listener);
    }

    public static void showColorSelectDialog(final Context context, String defaultValue,
            final ColorDialogCallback listener) {
        int color = Color.parseColor(defaultValue);
        showColorSelectDialog(context, ColorUtils.getAlpha(color), ColorUtils.removeAlpha(color), listener);
    }


    public static void showColorSelectDialog(final Context context, int defaultAlpha, int defaultValue,
            final ColorDialogCompletedCallback listener) {
        showColorSelectDialog(context,
                ColorPickDialog.TYPE_WHELL,
                defaultAlpha,
                defaultValue,
                ThemeResourcesManager.INSTANCE.getString(R.string.dialog_color_button_ok),
                ThemeResourcesManager.INSTANCE.getString(R.string.dialog_color_button_reset),
                ThemeResourcesManager.INSTANCE.getString(R.string.dialog_color_tips), true,
                new ColorDialogCallback() {
                    @Override
                    public void onPositiveClick(int result) {
                        listener.onSelectedColor(result);
                    }

                    @Override
                    public void onNegativeClick(int result) {
                        listener.onSelectedColor(result);
                    }

                    @Override
                    public void onCancel(int result) {
                        listener.onSelectedColor(result);
                    }
                });
    }

    public static void showColorSelectDialog(final Context context, String defaultValue,
            final ColorDialogCompletedCallback listener) {
        int color = Color.parseColor(defaultValue);
        showColorSelectDialog(context, ColorUtils.getAlpha(color), ColorUtils.removeAlpha(color), listener);
    }
}
