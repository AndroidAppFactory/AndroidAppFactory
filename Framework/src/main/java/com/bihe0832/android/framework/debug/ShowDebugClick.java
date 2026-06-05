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
        String result = getDebugInfo(ctx) + "其他信息: \\n" + getExtraInfo() + "\\n";
        DebugInfoUtils.sendInfo(ctx, result);
    }

    public String getDebugInfo(Context ctx) {
        return DebugInfoUtils.getDebugInfo(ctx);
    }

    public static String getBasicDebugInfo(Context ctx) {
        return DebugInfoUtils.getDebugInfo(ctx);
    }

    public static String getDebugVersionInfo(Context ctx, boolean needSpaceLine) {
        return DebugInfoUtils.getDebugVersionInfo(ctx, needSpaceLine);
    }

    public static String getDebugDeviceInfo(boolean needSpaceLine) {
        return DebugInfoUtils.getDebugDeviceInfo(needSpaceLine);
    }

    public static void sendInfo(Context ctx, String result) {
        DebugInfoUtils.sendInfo(ctx, result);
    }
}