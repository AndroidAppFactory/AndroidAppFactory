package com.bihe0832.android.lib.ui.dialog.senddata;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.MovementMethod;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.text.ClipboardUtil;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.ui.dialog.R;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.ui.toast.ToastUtil;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import java.util.Arrays;
import java.util.List;

/**
 * @author zixie code@bihe0832.com Created on 2019-09-26. Description: Description
 */
public class SendTextUtils {

    public static void sendInfo(final Context context, final String title, final String contentData,
            final String sendData, final String tips, final String positiveText, final boolean showDialog) {
        sendInfo(context, title, contentData, null, null, tips, sendData, positiveText, showDialog);
    }


    public static void sendInfoWithHTML(final Context context, final String title, final String content,
            final String tips,
            final String positiveText, final boolean showDialog) {
        sendInfoWithCharSequence(context, title,
                TextFactoryUtils.getSpannedTextByHtml(content), null, tips,
                positiveText, showDialog);
    }

    public static void sendInfoWithCharSequence(final Context context, final String title, CharSequence content,
            MovementMethod method, final String tipsData, final String positiveText, final boolean showDialog) {
        sendInfo(context, title, "", content, method, tipsData, content.toString(), positiveText, showDialog);
    }


    public static void sendInfo(final Context context, final String title, String contentString,
            CharSequence contentChar, MovementMethod method, final String tipsData, final String sendData,
            final String positiveText, final boolean showDialog) {
        if (!showDialog) {
            sendInfo(context, title, sendData);
            return;
        }

        final CommonDialog dialog = new CommonDialog(context);
        dialog.setTitle(title);
        if (!TextUtils.isEmpty(contentString)) {
            dialog.setContent(contentString);
        } else {
            dialog.setHtmlContent(contentChar, method);
        }
        dialog.setFeedBackContent(tipsData);
        logInfo("DEBUG", title, sendData);
        dialog.setShouldCanceled(true);
        dialog.setPositive(positiveText);
        dialog.setNegative(context.getString(R.string.com_bihe0832_share_item_copy_clipboard));
        dialog.setOnClickBottomListener(new OnDialogListener() {
            @Override
            public void onPositiveClick() {
                try {
                    sendInfo(context, title, sendData);
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNegativeClick() {
                try {
                    copyToClipboard(context, sendData);
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
        ToastUtil.showShort(context, context.getString(R.string.com_bihe0832_share_tips_copy_clipboard));
    }

    public static void sendInfo(final Context context, final String title, final String content) {
        logInfo("DEBUG", title, content);
        IntentUtils.sendTextInfo(context, title, content);
    }

    public static void logInfo(String tag, String... data) {
        logInfo(tag, Arrays.asList(data));
    }

    public static void logInfo(String tag, List<String> data) {
        StringBuilder builder = new StringBuilder();
        builder.append(" \n----- " + tag + " ------\n\n");
        boolean isFirst = true;
        for (String content : data) {
            builder.append(content + "\n");
            if (isFirst) {
                builder.append("\n");
                isFirst = false;
            }
        }
        builder.append("----------------------\n");
        ZLog.d("DEBUG", builder.toString());
    }
}
