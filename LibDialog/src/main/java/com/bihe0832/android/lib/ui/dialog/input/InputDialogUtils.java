/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 上午10:45
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/25 上午10:45
 *
 */

package com.bihe0832.android.lib.ui.dialog.input;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.ui.dialog.R;
import com.bihe0832.android.lib.ui.dialog.callback.DialogStringCallback;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2022/5/25.
 *         Description: Description
 */
public class InputDialogUtils {

    public static void showInputDialog(final Context context, final CommonDialog dialog, String titleName, String msg,
            String positive, String negtive, Boolean canCanceledOnTouchOutside, int inputType, String defaultValue,
            String hint, final DialogStringCallback listener) {
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                dialog.setTitle(titleName);
                dialog.setHtmlContent(msg);
                dialog.setPositive(positive);
                dialog.setNegative(negtive);
                dialog.setShouldCanceled(canCanceledOnTouchOutside);
                final EditText editText = new EditText(context);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                editText.setLayoutParams(params);
                editText.setSingleLine();
                editText.setInputType(inputType);
                editText.setPadding(
                        DisplayUtil.dip2px(context, 4f),
                        DisplayUtil.dip2px(context, 8f),
                        DisplayUtil.dip2px(context, 4f),
                        DisplayUtil.dip2px(context, 8f)
                );
                editText.setBackgroundColor(context.getResources().getColor(com.bihe0832.android.lib.aaf.res.R.color.com_bihe0832_dialog_hint));
                editText.setTextSize(10);
                editText.setTextColor(context.getResources().getColor(com.bihe0832.android.lib.aaf.res.R.color.com_bihe0832_dialog_bg));
                editText.setHintTextColor(context.getResources().getColor(com.bihe0832.android.lib.aaf.res.R.color.com_bihe0832_dialog_split));
                editText.setHint(hint);
                if (!TextUtils.isEmpty(defaultValue)) {
                    editText.requestFocus();
                    editText.setText(defaultValue);
                    editText.selectAll();
                }

                dialog.addViewToContent(editText);
                dialog.setOnClickBottomListener(new OnDialogListener() {
                    @Override
                    public void onPositiveClick() {
                        try {
                            listener.onPositiveClick(editText.getText().toString());
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNegativeClick() {
                        try {
                            listener.onNegativeClick(editText.getText().toString());
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancel() {
                        try {
                            listener.onCancel(editText.getText().toString());
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
}
