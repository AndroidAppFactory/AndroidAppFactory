package com.bihe0832.android.common.ipc.iservice;

import android.support.annotation.NonNull;

/**
 * Created by hardyshi on 10/28/2020.
 */
public interface IBinderProvider {
    void attach(@NonNull IBinderPool pool);
}
