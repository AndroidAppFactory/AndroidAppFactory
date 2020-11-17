package com.bihe0832.android.framework.debug;

import android.content.Context;
import android.os.Build;
import android.view.View;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.debug.DebugTools;
import com.bihe0832.android.framework.ZixieContext;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-12.
 * Description: Description
 */
public class ShowDebugClick implements View.OnClickListener{

    protected void onClickAction(){

    }

    protected String getExtraInfo(){
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
                onClickAction();
                mSecretNumber = 0;
            }
        } else {
            mSecretNumber = 0;
        }
    }

    private void sendInfo(Context ctx){
        String result = getDebugInfo() +  "其他信息: \n" + getExtraInfo() + "\n";
        DebugTools.sendInfo(ctx,"请转发给开发者", result,true);
    }

    public static final String getDebugInfo(){
        StringBuilder builder = new StringBuilder();
        builder.append("设备信息: " + "\n");
        builder.append("厂商&型号: "+  Build.MANUFACTURER + ", " + Build.MODEL+ "\n");
        builder.append("系统版本: "+  Build.VERSION.RELEASE + ", " + Build.VERSION.SDK_INT + "\n");
        builder.append("deviceId: "+  ZixieContext.INSTANCE.getDeviceId() + "\n");

        builder.append("版本信息: " + "\n");
        builder.append("version: " + ZixieContext.INSTANCE.getVersionName() + "." + ZixieContext.INSTANCE.getVersionCode() + "\n");
        builder.append("Tag: "+ ZixieContext.INSTANCE.getTag()+ "\n");
        builder.append("channel: "+ ZixieContext.INSTANCE.getChannelID()  + "\n");
        builder.append("official: " + ZixieContext.INSTANCE.isOfficial() + "\n\n");
        return builder.toString();
    }
}