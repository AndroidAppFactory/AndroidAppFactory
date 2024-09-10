package com.bihe0832.android.lib.utils.intent;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.bihe0832.android.lib.utils.os.BuildUtils;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/9/10.
 *         Description:
 */
public class PendingIntentUtils {

    public static PendingIntent getBroadcastPendingIntent(Context context, String action, Class<?> classT,
            Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, classT);
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        return getBroadcastPendingIntent(context, 0, intent);
    }

    public static PendingIntent getBroadcastPendingIntent(Context context, int requestCode, Intent intent) {

        //设置pendingIntent
        PendingIntent pendingIntent;
        if (BuildUtils.INSTANCE.getSDK_INT() >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }
        //Retrieve a PendingIntent that will perform a broadcast
        return pendingIntent;
    }


    public static PendingIntent getActivityPendingIntent(Context context, Class<? extends Activity> action,
            Bundle bundle, int requestCode) {
        final Intent activityIntent = new Intent(context, action);
        Bundle finalBundle = new Bundle();
        if (null != bundle) {
            finalBundle.putAll(bundle);
        }
        activityIntent.putExtras(finalBundle);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, requestCode, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getUriPendingIntent(Context context, String uri, int requestCode, boolean needNewTask) {
        final Intent activityIntent = new Intent();
        activityIntent.setData(Uri.parse(uri));
        if (needNewTask) {
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        return PendingIntent.getActivity(context, requestCode, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
