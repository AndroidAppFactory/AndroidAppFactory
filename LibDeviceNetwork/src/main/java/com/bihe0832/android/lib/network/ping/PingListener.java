package com.bihe0832.android.lib.network.ping;

public interface PingListener {
    void onPingTime(final String ip, final int pingTime);

    void onPingResult(final PingResult pingResult);
}
