package com.bihe0832.android.lib.theme

import android.app.Application
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.bihe0832.android.lib.language.MultiLanguageHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.IdGenerator


object ThemeResourcesManager {
    private var mThemeResources: Resources? = null
    private lateinit var mAppDefaultResources: Resources
    private var mThemePkgName: String? = null
    private var userAppDefaultResources = true
    private var mVersion = IdGenerator(0)

    fun init(application: Application) {
        val context = MultiLanguageHelper.modifyContextLanguageConfig(
            application,
            MultiLanguageHelper.getLanguageConfig(application)
        )
        mAppDefaultResources = context.resources
    }

    // 使用新主题
    fun apply(resources: Resources?, pkgName: String?) {
        ThemeChangedLiveData.postValue(mVersion.generate())
        userAppDefaultResources = TextUtils.isEmpty(pkgName) || resources == null
        if (!userAppDefaultResources) {
            mThemeResources = resources
            mThemePkgName = pkgName
        }
    }

    // 使用默认主题
    fun reset() {
        ThemeChangedLiveData.postValue(mVersion.generate())
        mThemeResources = null
        mThemePkgName = ""
        userAppDefaultResources = true
    }

    // 在皮肤包中不一定就是 当前程序的 id，所以先获取对应id 在当前的名称，然后再根据名称去皮肤获取id
    fun getResIdInTheme(resId: Int): Int {
        if (userAppDefaultResources) {
            return resId
        }
        val resName = mAppDefaultResources.getResourceEntryName(resId) //ic_launcher
        val resType = mAppDefaultResources.getResourceTypeName(resId) //drawable
        return mThemeResources?.getIdentifier(resName, resType, mThemePkgName) ?: 0
    }

    fun getColor(resId: Int): Int? {
        try {
            if (userAppDefaultResources) {
                return mAppDefaultResources.getColor(resId)
            }
            val resIdInTheme = getResIdInTheme(resId)
            return if (resIdInTheme == 0) {
                mAppDefaultResources.getColor(resId)
            } else {
                try {
                    return mThemeResources!!.getColor(resIdInTheme)
                } catch (e: Resources.NotFoundException) {
                    ZLog.e(
                        ThemeManager.TAG, "Resources.NotFoundException:  Theme $mThemePkgName,  R.${
                            mAppDefaultResources.getResourceTypeName(resId)
                        }.${mAppDefaultResources.getResourceEntryName(resId)}"
                    )
                    e.printStackTrace()
                    return mAppDefaultResources.getColor(resId)
                }
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            ZLog.e(
                ThemeManager.TAG, "Resources.NotFoundException: Theme Default,  R.${
                    mAppDefaultResources.getResourceTypeName(resId)
                }.${mAppDefaultResources.getResourceEntryName(resId)}"
            )
        }
        return null
    }

    fun getColorStateList(resId: Int): ColorStateList? {
        try {
            if (userAppDefaultResources) {
                return mAppDefaultResources.getColorStateList(resId)
            }
            val resIdInTheme = getResIdInTheme(resId)
            return if (resIdInTheme == 0) {
                mAppDefaultResources.getColorStateList(resId)
            } else {
                try {
                    return mThemeResources!!.getColorStateList(resIdInTheme)
                } catch (e: Resources.NotFoundException) {
                    e.printStackTrace()
                    ZLog.e(
                        ThemeManager.TAG, "Resources.NotFoundException:  Theme $mThemePkgName,  R.${
                            mAppDefaultResources.getResourceTypeName(resId)
                        }.${mAppDefaultResources.getResourceEntryName(resId)}"
                    )

                    return mAppDefaultResources.getColorStateList(resId)
                }
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            ZLog.e(
                ThemeManager.TAG, "Resources.NotFoundException: Theme Default,  R.${
                    mAppDefaultResources.getResourceTypeName(resId)
                }.${mAppDefaultResources.getResourceEntryName(resId)}"
            )

        }
        return null
    }

    fun getDrawable(resId: Int): Drawable? {
        try {
            if (userAppDefaultResources) {
                return mAppDefaultResources.getDrawable(resId)
            }
            val resIdInTheme = getResIdInTheme(resId)
            return if (resIdInTheme == 0) {
                mAppDefaultResources.getDrawable(resId)
            } else {
                try {
                    return mThemeResources!!.getDrawable(resIdInTheme)
                } catch (e: Resources.NotFoundException) {
                    e.printStackTrace()
                    ZLog.e(
                        ThemeManager.TAG, "Resources.NotFoundException:  Theme $mThemePkgName,  R.${
                            mAppDefaultResources.getResourceTypeName(resId)
                        }.${mAppDefaultResources.getResourceEntryName(resId)}"
                    )

                    return mAppDefaultResources.getDrawable(resId)
                }
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            ZLog.e(
                ThemeManager.TAG, "Resources.NotFoundException: Theme Default,  R.${
                    mAppDefaultResources.getResourceTypeName(resId)
                }.${mAppDefaultResources.getResourceEntryName(resId)}"
            )

        }
        return null
    }

    /**
     * 可能是Color 也可能是drawable
     *
     * @return
     */
    fun getBackground(resId: Int): Any? {
        val resourceTypeName = mAppDefaultResources.getResourceTypeName(resId)
        return if (resourceTypeName == "color") {
            getColor(resId)
        } else {
            getDrawable(resId)
        }
    }

    fun getString(resId: Int): String? {
        try {
            if (userAppDefaultResources) {
                return mAppDefaultResources.getString(resId)
            }
            val resIdInTheme = getResIdInTheme(resId)
            return if (resIdInTheme == 0) {
                mAppDefaultResources.getString(resId)
            } else {
                try {
                    return mThemeResources!!.getString(resIdInTheme)
                } catch (e: Resources.NotFoundException) {
                    e.printStackTrace()
                    ZLog.e(
                        ThemeManager.TAG, "Resources.NotFoundException:  Theme $mThemePkgName,  R.${
                            mAppDefaultResources.getResourceTypeName(resId)
                        }.${mAppDefaultResources.getResourceEntryName(resId)}"
                    )
                    return mAppDefaultResources.getString(resId)
                }
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            ZLog.e(
                ThemeManager.TAG, "Resources.NotFoundException: Theme Default,  R.${
                    mAppDefaultResources.getResourceTypeName(resId)
                }.${mAppDefaultResources.getResourceEntryName(resId)}"
            )

        }
        return null
    }

    fun getTypeface(resId: Int): Typeface {
        val typefacePath = getString(resId)
        if (TextUtils.isEmpty(typefacePath)) {
            return Typeface.DEFAULT
        }
        try {
            if (userAppDefaultResources) {
                return Typeface.createFromAsset(mAppDefaultResources.assets, typefacePath)
            }
            return Typeface.createFromAsset(mThemeResources!!.assets, typefacePath)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        return Typeface.DEFAULT
    }
}