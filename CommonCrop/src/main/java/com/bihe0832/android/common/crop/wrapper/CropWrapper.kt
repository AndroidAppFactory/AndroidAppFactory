package com.bihe0832.android.common.crop.wrapper

import android.content.Intent
import android.net.Uri
import com.bihe0832.android.common.crop.CropUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2024/12/13.
 * Description: Description
 *
 */
object CropWrapper {

    private var mCallback: AAFDataCallback<Uri>? = null

    fun getCallBack(): AAFDataCallback<Uri>? {
        return mCallback
    }

    fun startCrop(source: Uri, options: CropUtils.Options?, data: AAFDataCallback<Uri>) {
        if (ZixieContext.applicationContext == null) {
            data.onError(-2, "call ZixieContext init First")
        }
        val intent = CropUtils.getCropIntent(
            ZixieContext.applicationContext, source, options, CropTransActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mCallback = data
        ZixieContext.applicationContext!!.startActivity(intent)
    }


}