/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 上午10:43
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/3/28 下午9:42
 *
 */

package com.bihe0832.android.lib.color.picker.dialog;

import androidx.annotation.ColorInt;

public interface ColorDialogCompletedCallback {

    void onSelectedColor(@ColorInt int result);
}
