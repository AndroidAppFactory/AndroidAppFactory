package com.bihe0832.android.framework.update;

public interface UpdateListener {
    void onProgress(int total, int cur);

    void onStage(String stage);

    void onError(int error, String errmsg);
}
