package com.bihe0832.android.app.tools

import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils

/**
 * AAF 工具类
 *
 * 提供应用层通用工具方法
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/7/10.
 */
object AAFTools {

    /**
     * 获取剪切板内容
     *
     * 从系统剪切板读取文本内容
     *
     * @param context 上下文
     * @return 剪切板文本内容，如果为空则返回空字符串
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