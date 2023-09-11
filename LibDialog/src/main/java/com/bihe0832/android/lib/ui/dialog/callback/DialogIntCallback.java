/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 上午10:43
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/25 上午10:32
 *
 */

package com.bihe0832.android.lib.ui.dialog.callback;

public interface DialogIntCallback {

    void onPositiveClick(int result);

    void onNegativeClick(int result);

    void onCancel(int result);
}
