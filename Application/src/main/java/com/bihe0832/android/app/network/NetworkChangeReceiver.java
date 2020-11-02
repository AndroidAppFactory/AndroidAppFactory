package com.bihe0832.android.app.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bihe0832.android.lib.log.ZLog;


public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ZLog.d("NetworkChangeReceiver onReceive:" + intent.getAction());
        NetworkChangeManager.change(context.getApplicationContext(), intent);
    }
}
