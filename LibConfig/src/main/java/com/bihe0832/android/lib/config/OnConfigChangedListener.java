/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/26 下午8:10
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/26 下午8:10
 *
 */

package com.bihe0832.android.lib.config;

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/5/26.
 * Description: Description
 */
public interface OnConfigChangedListener {

    void onValueChanged(String key, String value);

    void onValueAgain(String key, String value);
}
