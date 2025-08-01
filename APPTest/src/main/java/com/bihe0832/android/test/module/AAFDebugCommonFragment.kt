/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:05
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.test.module

import android.content.Context
import com.bihe0832.android.common.compose.debug.DebugUtils
import com.bihe0832.android.common.compose.debug.module.DebugCommonComposeFragment

open class AAFDebugCommonFragment : DebugCommonComposeFragment() {

    override fun showLog(context: Context) {
        DebugUtils.startActivityWithException(context, AAFDebugLogListActivity::class.java)
    }

}