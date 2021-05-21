package com.bihe0832.android.common.ipc.iservice;

import android.os.IBinder;
import android.support.annotation.NonNull;


/**
 * Created by hardyshi on 10/28/2020.
 */
public interface IBinderPool {

    @NonNull
    IBinder getBinder(@NonNull String serviceInterfaceName);
}
