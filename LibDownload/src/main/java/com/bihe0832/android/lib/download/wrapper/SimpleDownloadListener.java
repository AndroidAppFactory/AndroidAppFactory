package com.bihe0832.android.lib.download.wrapper;

import com.bihe0832.android.lib.download.DownloadItem;
import com.bihe0832.android.lib.download.DownloadListener;
import com.bihe0832.android.lib.log.ZLog;
import org.jetbrains.annotations.NotNull;

/**
 * @author hardyshi code@bihe0832.com Created on 2/1/21.
 */
public abstract class SimpleDownloadListener implements DownloadListener {

    @Override
    public void onWait(@NotNull DownloadItem item) {
        ZLog.d("onWait" + item.toString());
    }

    @Override
    public void onStart(@NotNull DownloadItem item) {
        ZLog.d("onStart" + item.toString());
    }

    @Override
    public void onPause(@NotNull DownloadItem item) {
        ZLog.d("onPause" + item.toString());
    }

    @Override
    public void onDelete(@NotNull DownloadItem item) {
        ZLog.d("onDelete" + item.toString());
    }
}
