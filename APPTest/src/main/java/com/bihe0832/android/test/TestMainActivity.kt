package com.bihe0832.android.test

import android.Manifest
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.immersion.hideBottomUIMenu
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

        PermissionManager.addPermissionDesc(HashMap<String, String>().apply {
            put(Manifest.permission.CAMERA, "相机")
            put(Manifest.permission.RECORD_AUDIO, "录音")

        })

        PermissionManager.addPermissionScene(HashMap<String, String>().apply {
            put(Manifest.permission.CAMERA, "扫描二维码")
            put(Manifest.permission.RECORD_AUDIO, "语音录制")
        })

        PermissionManager.checkPermission(this, true, getPermissionResult(), Manifest.permission.CAMERA)
        UpdateManager.checkUpdateAndShowDialog(this, false)
        hideBottomUIMenu()
    }

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(this, R.color.white)
    }

    override fun getNavigationBarColor(): Int {
        return ContextCompat.getColor(this, R.color.result_point_color)
    }

    override fun getPermissionList(): List<String> {
        return ArrayList<String>().apply {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
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
//        hideBottomUIMenu()
    }


    override fun onBackPressedSupport() {
        super.onBackPressedSupport()
    }
}
