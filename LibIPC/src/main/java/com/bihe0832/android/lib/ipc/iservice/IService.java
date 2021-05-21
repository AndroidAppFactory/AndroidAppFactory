package com.bihe0832.android.lib.ipc.iservice;

import android.os.IBinder;
import android.os.IInterface;

/**
 * Created by hardyshi on 10/28/2020.
 */
public interface IService {

    IBinder getBinder();

    IInterface getInterface(IBinder binder);
}
