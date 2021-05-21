package com.bihe0832.android.lib.ipc.iservice;

import android.os.IBinder;


/**
 * Created by hardyshi on 10/28/2020.
 */
public interface IBinderPool {

    IBinder getBinder(String serviceInterfaceName);
}
