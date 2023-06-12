package com.bihe0832.android.common.debug.widget.app;

import com.bihe0832.android.lib.widget.BaseWidgetProvider;
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker;

public class AAFDebugWidgetProviderSimple extends BaseWidgetProvider {

    @Override
    public Class<? extends BaseWidgetWorker> getWidgetWorkerClass() {
        return AAFDebugWidgetWorkerSimple.class;
    }

    @Override
    protected boolean canAutoUpdateByOthers() {
        return true;
    }
}
