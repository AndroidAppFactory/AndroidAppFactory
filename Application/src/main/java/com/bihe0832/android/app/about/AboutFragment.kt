package com.bihe0832.android.app.about

import android.arch.lifecycle.Observer
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.bihe0832.android.app.R
import com.bihe0832.android.app.card.SettingsData
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.app.router.openWebPage
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.list.CardItemForCommonList
import com.bihe0832.android.framework.ui.list.CommonListLiveData
import com.bihe0832.android.framework.ui.list.swiperefresh.CommonListFragment
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.superapp.QQHelper
import com.bihe0832.android.lib.superapp.WechatOfficialAccount
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

/**
 * 如果有更新，第一个Item一定要是更新，否则会导致UI显示异常
 */
open class AboutFragment : CommonListFragment() {


    open fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getUpdate())
            add(getVersionList())
            add(getFeedback())
            add(getZixie())
            if (!ZixieContext.isOfficial()) {
                add(getDebug())
            }
        }
    }

    val mDataList by lazy {
        ArrayList<CardBaseModule>().apply {
            addAll(getDataList())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateRedPoint(UpdateInfoLiveData.value)
        UpdateInfoLiveData.observe(this, Observer<UpdateDataFromCloud> { data ->
            updateRedPoint(data)
        })
    }

    open fun updateRedPoint(cloud: UpdateDataFromCloud?) {
        if (mDataList.size > 0) {
            (mDataList[0] as SettingsData).apply {
                if(null != cloud && cloud.updateType > UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP){
                    mTipsText = "发现新版本"
                    mItemIsNew = true
                }else{
                    mTipsText = ""
                    mItemIsNew = false
                }
            }
            getAdapter().notifyDataSetChanged()
        }
    }


    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun fetchData() {
                postValue(mDataList)
            }

            override fun clearData() {

            }

            override fun loadMore() {

            }

            override fun hasMore(): Boolean {
                return false
            }

            override fun canRefresh(): Boolean {
                return false
            }

            override fun getEmptyText(): String {
                return ""
            }
        }
    }

    protected fun getVersionList(): SettingsData {
        return SettingsData("版本介绍").apply {
            mItemIconRes = R.mipmap.icon_help
            mHeaderTextBold = true
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                getString(R.string.version_url).let {url->
                    if(url.isNullOrEmpty()){
                        ZixieContext.showWaiting()
                    }else{
                        val map = HashMap<String, String>()
                        map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = Uri.encode(url)
                        RouterHelper.openPageRouter(RouterConstants.MODULE_NAME_WEB_PAGE, map)
                    }
                }
            }
        }
    }


    protected fun getUpdate(): SettingsData {
        return SettingsData("版本更新").apply {
            mItemIconRes = R.mipmap.icon_update
            mHeaderTextBold = true
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                activity?.let {
                    UpdateManager.checkUpdateAndShowDialog(it, true)
                }
            }
        }
    }


    protected fun getFeedback(): SettingsData {
        return SettingsData("建议反馈").apply {
            mItemIconRes = R.mipmap.icon_feedback
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                val map = HashMap<String, String>()
                map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = Uri.encode(getString(R.string.feedback_url))
                RouterHelper.openPageRouter(RouterConstants.MODULE_NAME_FEEDBACK, map)
            }
        }
    }

    protected fun getQQService(): SettingsData {
        return SettingsData("客服QQ").apply {
            var feedbackQQnumber = getString(R.string.feedback_qq)
            mItemIconRes = R.mipmap.icon_qq_black
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>${feedbackQQnumber}</u>"
            mHeaderTipsListener = View.OnClickListener {
                var res = QQHelper.openQQChat(activity, feedbackQQnumber)
                if (!res) {
                    ZixieContext.showToastJustAPPFront(getString(R.string.contact_QQ_join_failed))
                }
            }
        }
    }

    protected fun getDebug(): SettingsData {
        return SettingsData("调试").apply {
            mItemIconRes = R.mipmap.icon_author
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_DEBUG)
            }
        }
    }

    protected fun getWechat(): SettingsData {
        return SettingsData("微信公众号").apply {
            mItemIconRes = R.mipmap.icon_wechat_black
            mShowDriver = true
            mShowGo = true
            mTipsText = "<u>前往关注</u>"
            mHeaderTipsListener = View.OnClickListener {
                context?.let {
                    WechatOfficialAccount.showSubscribe(context, WechatOfficialAccount.WechatOfficialAccountData().apply {
                        this.mAccountID = getString(R.string.wechat_id)
                        this.mAccountTitle = getString(R.string.wechat_name)
                        this.mSubContent = getString(R.string.wechat_sub_content)
                    })
                }
            }
        }
    }

    protected fun getZixie(): SettingsData {
        return SettingsData("关于开发者").apply {
            mItemIconRes = R.mipmap.icon_author
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                openWebPage("file:///android_asset/web/author.html")
            }
        }
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf(
                CardItemForCommonList(SettingsData::class.java)
        )
    }
}