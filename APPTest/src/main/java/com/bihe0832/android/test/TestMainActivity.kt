package com.bihe0832.android.test

import android.os.Bundle
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.router.annotation.APPMain
import com.bihe0832.android.lib.router.annotation.Module


@APPMain
@Module("test")
class TestMainActivity : CommonActivity() {
    val LOG_TAG = "TestHttpActivity"
    var process = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar("AndroidAPPFactory", false)

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
