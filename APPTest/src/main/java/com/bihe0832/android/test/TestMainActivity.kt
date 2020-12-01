package com.bihe0832.android.test

import android.Manifest
import android.os.Bundle
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.router.annotation.APPMain
import com.bihe0832.android.lib.router.annotation.Module

@APPMain
@Module("test")
class TestMainActivity : CommonActivity() {
    val LOG_TAG = "TestHttpActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar("AndroidAPPFactory", false)
        CardInfoHelper.getInstance().setAutoAddItem(true)
    }

    override fun getPermissionList(): List<String> {
        return ArrayList<String>().apply {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun getPermissionResult(): PermissionManager.OnPermissionResult {
        return object : PermissionManager.OnPermissionResult {
            override fun onSuccess() {
                ZixieContext.showDebug("用户授权成功")
            }

            override fun onUserCancel() {
                ZixieContext.showLongToast("用户放弃授权")
            }

            override fun onUserDeny() {
                ZixieContext.showDebug("用户拒绝授权")
            }

            override fun onFailed(msg: String) {
                ZixieContext.showDebug("用户授权失败")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (findFragment(TestMainFragment::class.java) == null) {
            loadRootFragment(R.id.common_fragment_content, TestMainFragment.newInstance(0))
        }
        UpdateManager.checkUpdateAndShowDialog(this, false)
//        hideBottomUIMenu()
    }


    override fun onBackPressedSupport() {
        super.onBackPressedSupport()
    }
}
