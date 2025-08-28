package com.bihe0832.android.common.qrcode

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.bihe0832.android.common.compose.base.BaseComposeActivity
import com.bihe0832.android.common.compose.common.activity.FragmentContainer
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.state.ThemeState
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.common.qrcode.core.BaseScanFragment
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.router.annotation.Module
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.Locale

@Module(RouterConstants.MODULE_NAME_QRCODE_SCAN)
open class CommonScanActivity : BaseComposeActivity() {

    fun initPermission() {
        PermissionManager.addPermissionGroup(
            RouterConstants.MODULE_NAME_QRCODE_SCAN,
            Manifest.permission.CAMERA,
            AAFPermissionManager.takePhotoPermission,
        )
        PermissionManager.addPermissionGroupDesc(
            RouterConstants.MODULE_NAME_QRCODE_SCAN,
            Manifest.permission.CAMERA,
            getString(R.string.common_permission_title_camera),
        )
        PermissionManager.addPermissionGroupScene(
            RouterConstants.MODULE_NAME_QRCODE_SCAN,
            Manifest.permission.CAMERA,
            getString(R.string.common_permission_title_qrcode)
        )
    }

    override fun getActivityRootContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                val themeType by rememberUpdatedState(ThemeState.getCurrentThemeState())
                val maskColor = colorResource(R.color.viewfinder_mask)

                MaterialTheme(colorScheme = themeType) {
                    val systemUiController = rememberSystemUiController()
                    SideEffect {
                        systemUiController.setStatusBarColor(maskColor)
                        systemUiController.setNavigationBarColor(maskColor)
                    }
                    Surface {
                        FragmentContainer(
                            modifier = Modifier.fillMaxSize(), getScanFragment()
                        )
                    }
                }
            }
        }
    }

    override fun onLocaleChanged(lastLocale: Locale, toLanguageTag: Locale) {
        recreate()
    }

    open fun getScanFragment(): BaseScanFragment {
        return fragment
    }

    private val fragment by lazy {
        CommonScanFragment()
    }
}
