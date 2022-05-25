/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 上午10:43
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/25 上午10:32
 *
 */

package com.bihe0832.android.lib.ui.dialog.impl.input;

public interface InputDialogCallback {
    void onPositiveClick(String result);
    void onNegativeClick(String result);
    void onCancel(String result);
}
