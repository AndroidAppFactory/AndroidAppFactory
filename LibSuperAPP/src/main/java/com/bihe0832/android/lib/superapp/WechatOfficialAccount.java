package com.bihe0832.android.lib.superapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.ui.dialog.OnDialogListener;
import com.bihe0832.android.lib.ui.toast.ToastUtil;
import com.bihe0832.android.lib.utils.apk.APKUtils;

/**
 * @author hardyshi code@bihe0832.com
 *         Created on 2019-11-01.
 *         Description: Description
 */
public class WechatOfficialAccount {

    public static class WechatOfficialAccountData {

        public String mAccountID = "";
        public String mAccountTitle = "";
        public String mSubContent = "";
        public int mResID = -1;
        public View.OnClickListener mOnFinishedClickListener = null;

    }

    public static void showSubscribe(final Context context, final WechatOfficialAccountData data) {

        final CommonDialog dialog = new CommonDialog(context);
        dialog.setTitle("关注「" + data.mAccountTitle + "」公众号");
        if (TextUtils.isEmpty(data.mSubContent)) {
            dialog.setHtmlContent("微信玩家可以按照下方流程关注「" + data.mAccountTitle + "」微信公众号。");
        } else {
            dialog.setHtmlContent(data.mSubContent);
        }
        dialog.setImageContentResId(data.mResID);
        dialog.setFeedBackContent("点击「前往关注」会自动复制微信公众号ID并拉起微信");
        dialog.setPositive("前往关注");
        dialog.setNegative("已经关注");
        dialog.setCancelable(false);
        dialog.setOnClickBottomListener(new OnDialogListener() {
            @Override
            public void onPositiveClick() {
                try {
                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                        cm.setPrimaryClip(ClipData.newPlainText(null, data.mAccountID));
                    } else {
                        cm.setText(data.mAccountID);
                    }
                    ToastUtil.showShort(context, "公众账号信息已复制到剪贴板");
                    APKUtils.startApp(context, "微信", SuperAPPContants.APK_PACKAGE_NAME_WECHAT,
                            SuperAPPContants.APK_LAUNCHER_CLASS_WECHAT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNegativeClick() {
                try {
                    if (data.mOnFinishedClickListener != null) {
                        data.mOnFinishedClickListener.onClick(null);
                    }
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
}
