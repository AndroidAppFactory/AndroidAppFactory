/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:05
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.test.module

import com.bihe0832.android.common.debug.module.DebugCommonFragment

open class AAFDebugCommonFragment : DebugCommonFragment() {

    override fun showLog() {
        startActivity(AAFDebugLogActivity::class.java)
    }

}