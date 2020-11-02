package com.bihe0832.android.app.network.listener;

import android.content.Intent;

import com.bihe0832.android.lib.network.NetworkUtil;


/**
 * Created by phorusliao on 2017/5/8.
 */

public class NetworkChangeEvent {

    public int preNet;
    public int curNet;
    public int preDtType;
    public int curDtType;
    public String ssId;
    public String bssId;
    public int wifi_strength;
    public String cellId;
    public Intent intent;
    public NetworkUtil.DtTypeInfo curDtTypeInfo;

    public NetworkChangeEvent(int preNet, int curNet, int preDtType, int curDtType,
        String ssId, String bssId, int wifi_strength,
        String cellId, Intent intent, NetworkUtil.DtTypeInfo curDtTypeInfo) {
        this.preNet = preNet;
        this.curNet = curNet;
        this.preDtType = preDtType;
        this.curDtType = curDtType;
        this.ssId = ssId;
        this.bssId = bssId;
        this.wifi_strength = wifi_strength;
        this.cellId = cellId;
        this.intent = intent;
        this.curDtTypeInfo = curDtTypeInfo;
    }

    @Override
    public String toString() {
        return "NetworkChangeEvent{" +
                "preNet=" + preNet +
                ", curNet=" + curNet +
                ", preDtType=" + preDtType +
                ", curDtType=" + curDtType +
                ", ssId='" + ssId + '\'' +
                ", bssId='" + bssId + '\'' +
                ", wifi_strength=" + wifi_strength +
                ", cellId='" + cellId + '\'' +
                ", intent=" + intent +
                ", curDtTypeInfo=" + curDtTypeInfo +
                '}';
    }
}
