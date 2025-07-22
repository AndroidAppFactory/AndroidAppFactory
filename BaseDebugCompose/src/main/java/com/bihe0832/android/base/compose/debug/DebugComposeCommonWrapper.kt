package com.bihe0832.android.base.compose.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.bihe0832.android.base.compose.debug.common.DebugComposeConfigView
import com.bihe0832.android.base.compose.debug.language.DebugComposeLanguageView
import com.bihe0832.android.base.compose.debug.list.DebugComposeListView
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */

class DebugComposeViewPreviewParameterProvider : PreviewParameterProvider<Locale> {
    override val values = sequenceOf(Locale.CHINESE, Locale.US)
}

@Preview(showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun DebugComposeViewPreview(
    @PreviewParameter(DebugComposeViewPreviewParameterProvider::class) currentLanguage: Locale
) {
    GetCommonDebug(
        AAFDebugCompose.DebugCommonCompose + "LanguageView", currentLanguage
    )
}

@Composable
fun GetCommonDebug(viewKey: String, currentLanguage: Locale): Boolean {
    when (viewKey) {
        AAFDebugCompose.DebugCommonCompose + "ComposeView" -> {
            DebugComposeView()
            return true
        }

        AAFDebugCompose.DebugCommonCompose + "ConfigView" -> {
            DebugComposeConfigView(currentLanguage)
            return true
        }

        AAFDebugCompose.DebugCommonCompose + "LanguageView" -> {
            DebugComposeLanguageView(currentLanguage)
            return true
        }

        AAFDebugCompose.DebugCommonCompose + "ListView" -> {
            DebugComposeListView()
            return true
        }
    }
    return false
}

