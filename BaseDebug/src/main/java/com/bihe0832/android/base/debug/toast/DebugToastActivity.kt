package com.bihe0832.android.base.debug.toast

import android.os.Bundle
import android.widget.Toast
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.ui.toast.ToastUtil

class DebugToastActivity : CommonActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        ZixieContext.showToast("这是DebugToastActivity测试")
        ToastUtil.showTips(ZixieContext.applicationContext, R.mipmap.icon,"fsdfdsf",Toast.LENGTH_LONG)
        finish()
    }
}