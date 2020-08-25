package com.bihe0832.android.lib.ui.view.ext;

import android.text.InputType
import android.widget.EditText

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2019-09-17.
 * Description: Description
 *
 */
/**
 * 设置EditView是否可编辑
 */
fun EditText.setEditTextEnable(isEditable: Boolean) {
    isFocusable = isEditable
    isFocusableInTouchMode = isEditable
    isLongClickable = isEditable
    inputType = if (isEditable) InputType.TYPE_CLASS_TEXT else InputType.TYPE_NULL
}