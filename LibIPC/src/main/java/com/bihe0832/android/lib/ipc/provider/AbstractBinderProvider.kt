package com.bihe0832.android.lib.ipc.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import com.bihe0832.android.lib.ipc.ServiceManager
import com.bihe0832.android.lib.ipc.iservice.IBinderPool
import com.bihe0832.android.lib.ipc.iservice.IBinderProvider
import com.bihe0832.android.lib.log.ZLog

/**
 * Created by hardyshi on 10/27/2020.
 */
abstract class AbstractBinderProvider : ContentProvider(),
        IBinderProvider {

    private lateinit var binderPool: IBinderPool

    override fun onCreate(): Boolean {
       ZLog.d("ServiceManager", "BinderProvider[$this].onCreate")
        ServiceManager.attach(this)
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
       ZLog.d("ServiceManager", "BinderProvider[$this].call:$method $arg $extras")

        val binder = getBinder(arg!!)
       ZLog.d("ServiceManager", "BinderProvider[$this].call: binder:$binder")
        return Bundle().apply {
            putBinder("binder", binder)
        }
    }

    override fun attach(pool: IBinderPool) {
        binderPool = pool
    }

    private fun getBinder(serviceInterfaceName: String): IBinder {
        return binderPool.getBinder(serviceInterfaceName)
    }


    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(
            uri: Uri,
            projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }
}