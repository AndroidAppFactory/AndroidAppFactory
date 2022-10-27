package com.bihe0832.android.lib.debug;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.method.MovementMethod;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.text.ClipboardUtil;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.ui.dialog.OnDialogListener;
import com.bihe0832.android.lib.ui.toast.ToastUtil;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-09-26.
 * Description: Description
 */
public class DebugTools {

    public static void sendInfo(final Context context, final String title, final String content,
                                final boolean showDialog) {
        if (!showDialog) {
            sendInfo(context, title, content);
            return;
        }
        final CommonDialog dialog = new CommonDialog(context);
        dialog.setTitle(title);
        dialog.setPositive("分享给我们");
        dialog.setContent("调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们");
        dialog.setImageContentResId(R.mipmap.debug);
        dialog.setFeedBackContent("我们承诺你提供的信息仅用于问题定位");
        dialog.setNegative("复制到剪切板");
        dialog.setOnClickBottomListener(new OnDialogListener() {
            @Override
            public void onPositiveClick() {
                try {
                    sendInfo(context, title, content);
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNegativeClick() {
                try {
                    copyToClipboard(context, content);
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel() {

            }
        });
        dialog.show();
    }

    public static void showInfoWithHTML(final Context context, final String title, final String content,
                                        final String positiveText) {
        showInfoWithCharSequence(context, title, TextFactoryUtils.getSpannedTextByHtml(content), null, positiveText);
    }

    public static void showInfo(final Context context, final String title, final String content,
                                final String positiveText) {
        showInfo(context, title, content, null, null, positiveText);
    }

    public static void showInfoWithCharSequence(final Context context, final String title, CharSequence content,
                                                MovementMethod method, final String positiveText) {
        showInfo(context, title, "", content, method, positiveText);
    }


    public static void showInfo(final Context context, final String title, final String content,
                                CharSequence charSequence, MovementMethod method, final String positiveText) {

        ZLog.d("DEBUG", "----- showInfo ------");
        ZLog.d("DEBUG", title);
        ZLog.d("DEBUG", content);
        ZLog.d("DEBUG", "----------------------");

        final CommonDialog dialog = new CommonDialog(context);
        String tempContent = "";
        if (!TextUtils.isEmpty(content)) {
            dialog.setContent(content);
            tempContent = content;
        } else {
            dialog.setHtmlContent(charSequence, method);
            tempContent = charSequence.toString();
        }
        final String finalContent = tempContent;
        dialog.setTitle(title)
                .setShouldCanceled(true)
                .setPositive(positiveText)
                .setNegative("复制到剪切板")
                .setOnClickBottomListener(new OnDialogListener() {
                    @Override
                    public void onPositiveClick() {
                        try {
                            sendInfo(context, title, finalContent);
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNegativeClick() {
                        try {
                            copyToClipboard(context, finalContent);
                            dialog.dismiss();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
        dialog.show();
    }

    public static void copyToClipboard(Context context, String content) {
        ClipboardUtil.copyToClipboard(context, content);
        ToastUtil.showShort(context, "信息已保存到剪贴板");
    }

    public static void sendInfo(final Context context, final String title, final String content) {

        ZLog.d("DEBUG", "----- sendInfo ------");
        ZLog.d("DEBUG", title);
        ZLog.d("DEBUG", content);
        ZLog.d("DEBUG", "----------------------");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        sendIntent.setType("text/plain");
        try {
            context.startActivity(Intent.createChooser(sendIntent, title));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                context.startActivity(sendIntent);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }
}
