package com.bihe0832.android.common.qrcode

import com.bihe0832.android.common.qrcode.core.BaseScanFragment
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.router.annotation.Module

@Module(RouterConstants.MODULE_NAME_QRCODE_SCAN_AND_PARSE)
open class CommonWithResultActivity : CommonScanActivity() {

    private val fragment by lazy {
        CommonWithResultFragment()
    }

    override fun getScanFragment(): BaseScanFragment {
        return fragment
    }
}
