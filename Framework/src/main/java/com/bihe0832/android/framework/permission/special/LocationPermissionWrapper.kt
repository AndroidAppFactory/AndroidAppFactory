package com.bihe0832.android.framework.permission.special

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import com.bihe0832.android.lib.ui.dialog.CommonDialog

/**
 *
 * @author zixie code@bihe0832.com Created on 12/24/20.
 *
 */

object LocationPermissionWrapper {


    fun getEnabledDialog(activity: Activity, content: String): CommonDialog {
        return CommonDialog(activity).apply {
            setTitle("开启位置服务")
            setHtmlContent(content)
            setPositive("开启位置服务")
            setNegative("暂不开启")
            setShouldCanceled(true)
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }



}
