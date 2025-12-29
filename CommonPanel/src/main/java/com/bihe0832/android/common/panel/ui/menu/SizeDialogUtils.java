/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 上午10:45
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/25 上午10:45
 *
 */

package com.bihe0832.android.common.panel.ui.menu;

import android.content.Context;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedIntCallback;
import com.bihe0832.android.lib.ui.dialog.callback.DialogIntCallback;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2022/5/25.
 *         Description: Description
 */
public class SizeDialogUtils {

    public static final int DEFAULT_PICK_DP = 160;

    public static void showSizeSelectDialog(final Context context, String title,
            int selectType, int defaultValue, int maxValue,
            String positive, String negative, Boolean canCanceledOnTouchOutside, final DialogIntCallback listener) {
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                final SizeSelectDialog dialog = new SizeSelectDialog(context, selectType);
                dialog.setTitle(title);
                dialog.setSize(defaultValue);
                dialog.setMinAndMax(maxValue);
                dialog.setPositive(positive);
                dialog.setNegative(negative);
                dialog.setShouldCanceled(canCanceledOnTouchOutside);
                dialog.setOnClickBottomListener(new OnDialogListener() {
                    @Override
                    public void onPositiveClick() {
                        try {
                            listener.onPositiveClick(dialog.getCurrent());
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
                            listener.onCancel(dialog.getCurrent());
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

    public static void showSizeSelectDialog(final Context context, int selectType,
            int defaultValue, int maxValue,
            final DialogIntCallback listener) {
        String title = "线条粗细";
        if (selectType == SizeSelectDialog.TYPE_TEXT_SIZE) {
            title = "文字大小";
        }
        showSizeSelectDialog(context, title, selectType, defaultValue, maxValue,
                ThemeResourcesManager.INSTANCE.getString(
                        com.bihe0832.android.model.res.R.string.dialog_color_button_ok), ThemeResourcesManager.INSTANCE.getString(
                        com.bihe0832.android.model.res.R.string.dialog_color_button_reset), true, listener);
    }


    public static void showSizeSelectDialog(final Context context, int selectType, int defaultValue, int maxValue,
            final DialogCompletedIntCallback listener) {
        showSizeSelectDialog(context, selectType, defaultValue, maxValue, new DialogIntCallback() {
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

    public static void showLineSizeSelectDialog(final Context context, int defaultValue, int maxValue,
            final DialogCompletedIntCallback listener) {
        showSizeSelectDialog(context, SizeSelectDialog.TYPE_LINE, defaultValue, maxValue, listener);
    }

    public static void showTextSizeSelectDialog(final Context context, int defaultValue, int maxValue,
            final DialogCompletedIntCallback listener) {
        showSizeSelectDialog(context, SizeSelectDialog.TYPE_TEXT_SIZE, defaultValue, maxValue, listener);
    }
}
