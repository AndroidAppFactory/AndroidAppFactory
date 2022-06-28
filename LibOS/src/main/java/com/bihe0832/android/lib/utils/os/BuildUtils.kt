package com.bihe0832.android.lib.utils.os

import android.os.Build
import com.bihe0832.android.lib.utils.ConvertUtils

/**
 * Created by zixie on 2017/10/31.
 */
object BuildUtils {


    val RELEASE: String by lazy {
        ManufacturerUtil.getValueByKey("ro.build.version.release") { Build.VERSION.RELEASE }
    }

    val INCREMENTAL: String by lazy {
        ManufacturerUtil.getValueByKey("ro.build.version.incremental") { Build.VERSION.INCREMENTAL }
    }

    val BASE_OS: String by lazy {
        ManufacturerUtil.getValueByKey("ro.build.version.base_os") { Build.VERSION.BASE_OS }
    }

    val SDK: String by lazy {
        ManufacturerUtil.getValueByKey("ro.build.version.sdk") { Build.VERSION.SDK }
    }

    val SDK_INT: Int by lazy {
        ConvertUtils.parseInt(SDK, 0)
    }

    val DISPLAY : String by lazy {
        ManufacturerUtil.getValueByKey("ro.build.display.id") { Build.DISPLAY }
    }
}