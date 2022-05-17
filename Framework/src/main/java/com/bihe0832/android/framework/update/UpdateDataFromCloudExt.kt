package com.bihe0832.android.framework.update

import android.text.TextUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.utils.ConvertUtils

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/5/14.
 * Description: Description
 *
 */


fun UpdateDataFromCloud.setUpdateType() {
    val isApk = newVersionURL.endsWith(".apk")
    val hasMd5 = !TextUtils.isEmpty(newVersionMD5)

    updateType =
            if (!TextUtils.isEmpty(newVersionURL)) {
                if (isSpecialVersion(forceUpdateList)) {
                    // 是否强更
                    if (isApk && hasMd5) {
                        UpdateDataFromCloud.UPDATE_TYPE_MUST
                    } else if (!isApk) {
                        UpdateDataFromCloud.UPDATE_TYPE_MUST_JUMP
                    } else {
                        UpdateDataFromCloud.UPDATE_TYPE_NEW
                    }
                } else if (isSpecialVersion(needUpdateList)) {
                    // 是否弹窗
                    if (isApk && hasMd5) {
                        UpdateDataFromCloud.UPDATE_TYPE_NEED
                    } else if (!isApk) {
                        UpdateDataFromCloud.UPDATE_TYPE_NEED_JUMP
                    } else {
                        UpdateDataFromCloud.UPDATE_TYPE_NEW
                    }
                } else if (ZixieContext.getVersionCode() < newVersionCode) {
                    when {
                        // 是否特殊强更
                        ZixieContext.getVersionCode() <= forceUpdateMinVersionCode -> {
                            if (isApk && hasMd5) {
                                UpdateDataFromCloud.UPDATE_TYPE_MUST
                            } else if (!isApk) {
                                UpdateDataFromCloud.UPDATE_TYPE_MUST_JUMP
                            } else {
                                UpdateDataFromCloud.UPDATE_TYPE_NEW
                            }
                        }
                        // 是否特殊弹窗
                        ZixieContext.getVersionCode() <= needUpdateMinVersionCode -> {
                            if (isApk && hasMd5) {
                                UpdateDataFromCloud.UPDATE_TYPE_NEED
                            } else if (!isApk) {
                                UpdateDataFromCloud.UPDATE_TYPE_NEED_JUMP
                            } else {
                                UpdateDataFromCloud.UPDATE_TYPE_NEW
                            }
                        }
                        // 是否红点
                        ZixieContext.getVersionCode() <= showRedMaxVersionCode -> {
                            if (isApk && hasMd5) {
                                UpdateDataFromCloud.UPDATE_TYPE_RED
                            } else if (!isApk) {
                                UpdateDataFromCloud.UPDATE_TYPE_RED_JUMP
                            } else {
                                UpdateDataFromCloud.UPDATE_TYPE_NEW
                            }
                        }
                        // 无任何提示
                        else -> {
                            if (isApk && hasMd5) {
                                UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW
                            } else if (!isApk) {
                                UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP
                            } else {
                                UpdateDataFromCloud.UPDATE_TYPE_NEW
                            }
                        }
                    }
                } else {
                    UpdateDataFromCloud.UPDATE_TYPE_NEW
                }
            } else {
                UpdateDataFromCloud.UPDATE_TYPE_NEW
            }
}


fun isSpecialVersion(specialList: String): Boolean {
    if (!TextUtils.isEmpty(specialList)) {
        val list = specialList.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (version in list) {
            if (ZixieContext.getVersionCode() == ConvertUtils.parseLong(version, 0)) {
                return true
            }
        }
    }
    return false
}
