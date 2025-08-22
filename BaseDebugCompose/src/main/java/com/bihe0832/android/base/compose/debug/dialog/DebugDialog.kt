package com.bihe0832.android.base.compose.debug.dialog

import android.content.Context
import com.bihe0832.android.lib.ui.dialog.impl.RadioDialog

/**
 * @author zixie code@bihe0832.com Created on 7/20/21.
 */
class DebugDialog : RadioDialog {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, themeResId: Int) : super(context, themeResId) {}
}
