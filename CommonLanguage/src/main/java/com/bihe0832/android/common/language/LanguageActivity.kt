package com.bihe0832.android.common.language

import com.bihe0832.android.common.language.card.SettingsDataLanguage
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListActivity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.language.MultiLanguageHelper
import com.bihe0832.android.lib.language.MultiLanguageHelper.TAG
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.annotation.Module
import java.util.Locale

/**
 * @author zixie code@bihe0832.com Created on 2025/2/17. Description: Description
 */
@Module(RouterConstants.MODULE_NAME_LANGUAGE)
open class LanguageActivity : CommonListActivity() {

    val mDataList = ArrayList<CardBaseModule>()

    override fun supportMultiLanguage(): Boolean {
        return true
    }

    override fun getTitleText(): String {
        return resources.getString(R.string.settings_language_title)
    }

    override fun initView() {
        super.initView()
        mAdapter.apply {
            setOnItemClickListener { baseQuickAdapter, view, i ->
                setLocale((baseQuickAdapter.data[i] as? SettingsDataLanguage))
            }
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun canRefresh(): Boolean {
                return false
            }

            override fun hasMore(): Boolean {
                return false
            }

            override fun initData() {
                mDataList.clear()
                mDataList.addAll(getTempData())
                postValue(mDataList)
            }

            override fun loadMore() {

            }

            override fun refresh() {
                initData()
            }
        }
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(SettingsDataLanguage::class.java, true))
        }
    }

    open fun setLocale(settingData: SettingsDataLanguage?) {
        if (null != settingData?.locale) {
            updateApplicationLocale(settingData.locale!!)
            onLocaleChanged(getLastLocale(), settingData.locale!!)
        } else {
            ZixieContext.showToast(this@LanguageActivity.resources.getString(R.string.toast_settings_language_tips))
        }
    }

    fun updateApplicationLocale(locale: Locale) {
        MultiLanguageHelper.setLanguageConfig(this, locale)
        ZixieContext.updateApplicationContext(this, true)
        MultiLanguageHelper.modifyContextLanguageConfig(resources, locale)
    }

    override fun onLocaleChanged(lastLocale: Locale, newLocale: Locale) {
        super.onLocaleChanged(lastLocale, newLocale)
        iniToolBar()
        mDataLiveData.refresh()
    }

    fun getLanguageItem(titleName: String, localeInfo: Locale): SettingsDataLanguage {
        return SettingsDataLanguage().apply {
            this.title = titleName
            this.locale = localeInfo
        }
    }

    open fun getTempData(): List<CardBaseModule> {
        return mutableListOf<CardBaseModule>().apply {
            add(getLanguageItem("中文", Locale.CHINESE))
            add(getLanguageItem("English", Locale.US))
        }
    }
}
