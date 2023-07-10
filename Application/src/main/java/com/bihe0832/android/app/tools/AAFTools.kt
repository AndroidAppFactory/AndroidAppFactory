package com.bihe0832.android.app.tools

import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/7/10.
 * Description: Description
 */
object AAFTools {
    /**
     * 获取剪切板内容
     *
     * @return
     */
    fun pasteFromClipboard(context: Context?): String {
        val manager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (manager != null) {
            if (manager.hasPrimaryClip() && manager.primaryClip!!.itemCount > 0) {
                val addedText = manager.primaryClip!!.getItemAt(0).text
                val addedTextString = addedText.toString()
                if (!TextUtils.isEmpty(addedTextString)) {
                    return addedTextString
                }
            }
        }
        return ""
    }
}