package com.bihe0832.android.lib.text;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;

/**
 * @author zixie code@bihe0832.com Created on 6/21/21.
 */
public class ClipboardUtil {

    /**
     * 复制内容到剪切板
     *
     * @return
     */
    public static void copyToClipboard(Context context, String content) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.GINGERBREAD_MR1) {
            cm.setPrimaryClip(ClipData.newPlainText(null, content));
        } else {
            cm.setText(content);
        }
    }



    /**
     * 清空剪切板
     */
    public static void clearClipboard(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(ClipData.newPlainText("", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
