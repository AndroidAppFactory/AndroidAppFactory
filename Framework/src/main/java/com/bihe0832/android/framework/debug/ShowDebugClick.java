package com.bihe0832.android.framework.debug;

import android.content.Context;
import android.view.View;
import com.bihe0832.android.framework.R;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.lifecycle.LifecycleHelper;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.ui.dialog.senddata.SendTextUtils;
import com.bihe0832.android.lib.utils.apk.APKUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.utils.os.ManufacturerUtil;
import com.bihe0832.android.lib.utils.time.DateUtil;

/**
 * @author zixie code@bihe0832.com Created on 2019-11-12. Description: Description
 */
public class ShowDebugClick implements View.OnClickListener {

    protected void onClickAction() {

    }

    protected void onDebugAction() {

    }

    protected String getExtraInfo() {
        return "";
    }

    private static final String TAG = "ShowDebugClick";
    private static final long MIN_CLICK_INTERVAL = 500;
    private long mLastClickTime;
    private int mSecretNumber = 0;

    @Override
    public void onClick(View v) {
        long currentClickTime = System.currentTimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;
        ZLog.d(TAG + "currentClickTime:" + currentClickTime);
        ZLog.d(TAG + "mLastClickTime:" + mLastClickTime);
        ZLog.d(TAG + "elapsedTime:" + elapsedTime);
        ZLog.d(TAG + "duration:" + MIN_CLICK_INTERVAL);
        ZLog.d(TAG + "num:" + mSecretNumber);
        mLastClickTime = currentClickTime;
        if (elapsedTime < MIN_CLICK_INTERVAL) {
            mSecretNumber++;
            if (4 == mSecretNumber) {
                sendInfo(v.getContext());
                onDebugAction();
                mSecretNumber = 0;
            }
        } else {
            mSecretNumber = 0;
        }
        onClickAction();
    }

    protected void sendInfo(Context ctx) {
        String result = getDebugInfo(ctx) + "其他信息: \n" + getExtraInfo() + "\n";
        sendInfo(ctx, result);
    }

    public String getDebugInfo(Context ctx) {
        return getBasicDebugInfo(ctx);
    }

    public static String getBasicDebugInfo(Context ctx) {
        StringBuilder builder = new StringBuilder();
        builder.append(getDebugVersionInfo(ctx, true));
        builder.append(getDebugDeviceInfo(true));
        return builder.toString();
    }

    public static String getDebugVersionInfo(Context ctx, boolean needSpaceLine) {
        StringBuilder builder = new StringBuilder();
        builder.append("版本信息: " + "\n");
        builder.append(
                "应用版本: " + ZixieContext.INSTANCE.getVersionName() + "." + ZixieContext.INSTANCE.getVersionCode()
                        + "\n");
        builder.append("版本标识: " + ZixieContext.INSTANCE.getVersionTag() + "\n");
        builder.append("安装时间:" + DateUtil.getDateEN(LifecycleHelper.INSTANCE.getVersionInstalledTime()) + "\n");
        builder.append("channel: " + ZixieContext.INSTANCE.getChannelID() + "\n");
        builder.append("签名MD5: " + APKUtils.getSigMd5ByPkgName(ctx, ctx.getPackageName()) + "\n");
        builder.append("official: " + ZixieContext.INSTANCE.isOfficial() + "\n");
        if (needSpaceLine) {
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String getDebugDeviceInfo(boolean needSpaceLine) {
        StringBuilder builder = new StringBuilder();
        builder.append("设备信息: " + "\n");
        builder.append("厂商&型号: " + ManufacturerUtil.INSTANCE.getMANUFACTURER() + ", "
                + ManufacturerUtil.INSTANCE.getMODEL() + "\n");
        if (ManufacturerUtil.INSTANCE.isHarmonyOs()) {
            builder.append("系统版本: Android " + BuildUtils.INSTANCE.getRELEASE() + ", API "
                    + BuildUtils.INSTANCE.getSDK_INT() + ", Harmony(" + ManufacturerUtil.INSTANCE.getHarmonyVersion()
                    + ")\n");
        } else {
            builder.append("系统版本: Android " + BuildUtils.INSTANCE.getRELEASE() + ", API "
                    + BuildUtils.INSTANCE.getSDK_INT() + "\n");
        }
        builder.append("系统指纹: " + ManufacturerUtil.INSTANCE.getFINGERPRINT() + "\n");
        builder.append("设备标识: " + ZixieContext.INSTANCE.getDeviceId() + "\n");
        if (needSpaceLine) {
            builder.append("\n");
        }
        return builder.toString();
    }

    public static void sendInfo(Context ctx, String result) {
        SendTextUtils.sendInfo(ctx, ctx.getString(com.bihe0832.android.model.res.R.string.com_bihe0832_share_to_develop_title),
                ctx.getString(com.bihe0832.android.model.res.R.string.com_bihe0832_share_to_develop_content), result,
                ctx.getString(com.bihe0832.android.model.res.R.string.com_bihe0832_share_to_develop_tips),
                ctx.getString(com.bihe0832.android.model.res.R.string.com_bihe0832_share_to_develop), true);
    }
}