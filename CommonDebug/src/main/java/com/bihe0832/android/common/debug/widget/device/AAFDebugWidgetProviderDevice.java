package com.bihe0832.android.common.debug.widget.device;

import com.bihe0832.android.common.debug.widget.app.AAFDebugWidgetWorkerSimple;
import com.bihe0832.android.lib.widget.BaseWidgetProvider;
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker;

public class AAFDebugWidgetProviderDevice extends BaseWidgetProvider {

    @Override
    public Class<? extends BaseWidgetWorker> getWidgetWorkerClass() {
        return AAFDebugWidgetWorkerDevice.class;
    }

    @Override
    protected boolean canAutoUpdateByOthers() {
        return true;
    }
}
