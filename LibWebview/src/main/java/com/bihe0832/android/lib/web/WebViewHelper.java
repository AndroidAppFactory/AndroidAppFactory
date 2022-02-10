package com.bihe0832.android.lib.web;

import android.content.Context;
import android.content.Intent;
import com.bihe0832.android.lib.log.ZLog;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import java.util.HashMap;

public class WebViewHelper {

    private static final String TAG = "WebViewHelper";

    public static void init(Context context, boolean needPreLauncher) {
        initX5(context);
        if (needPreLauncher && null != context) {
            Intent intent = new Intent();
            intent.setClass(context, WebViewService.class);
            context.startService(intent);
        }
    }

    /**
     * 初始化X5
     *
     * @param context
     */
    private static void initX5(Context context) {
        if (null == context) {
            throw new IllegalArgumentException("initX5 error, context is null!");
        }

        HashMap<String, Object> map = new HashMap<String, Object>();
        // 启用X5私有ClassLoader
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_PRIVATE_CLASSLOADER, true);
        // 启用X5多进程dex2oat模式
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        QbSdk.initTbsSettings(map);

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                ZLog.d(TAG + "onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                ZLog.d(TAG + "onCoreInitFinished");
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(context.getApplicationContext(), cb);
    }
}
