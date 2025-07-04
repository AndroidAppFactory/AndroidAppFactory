package com.bihe0832.android.base.compose.debug.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.bihe0832.android.base.compose.debug.AAFDebugCompose
import com.bihe0832.android.base.compose.debug.common.item.DebugCommonConfigView
import com.bihe0832.android.base.compose.debug.common.item.DebugLanguageView
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */

class LocaleParameterProvider : PreviewParameterProvider<Locale> {
    override val values = sequenceOf(Locale.CHINESE, Locale.US)
}

@Preview(showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun DebugCommonConfigViewPreview(
    @PreviewParameter(LocaleParameterProvider::class) currentLanguage: Locale
) {
    GetCommonDebug(
        AAFDebugCompose.DebugCommonCompose + "LanguageView",
        currentLanguage
    )
}

@Composable
fun GetCommonDebug(viewKey: String, currentLanguage: Locale): Boolean {
    when (viewKey) {
        AAFDebugCompose.DebugCommonCompose + "ConfigView" -> {
            DebugCommonConfigView(currentLanguage)
            return true
        }

        AAFDebugCompose.DebugCommonCompose + "LanguageView" -> {
            DebugLanguageView(currentLanguage)
            return true
        }
    }
    return false
}

