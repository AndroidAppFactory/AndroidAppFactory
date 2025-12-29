package com.bihe0832.android.common.message.list

import android.app.Activity
import android.view.View
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListFragment
import com.bihe0832.android.common.message.R
import com.bihe0832.android.common.message.base.MessageManager
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.common.message.list.card.MessageItemData
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.custom.view.PlaceholderView
import com.bihe0832.android.model.res.R as ModelResR


abstract class BaseMessageFragment : CommonListFragment() {

    abstract fun getMessageManager(): MessageManager
    abstract fun showMessage(activity: Activity, messageInfoItem: MessageInfoItem, showFace: Boolean)

    override fun initView(view: View) {
        super.initView(view)
        getMessageManager().getMessageLiveData().observe(this) {
            mDataLiveData.initData()
        }

        mAdapter.apply {
            setOnItemChildClickListener { baseQuickAdapter, view, i ->
                baseQuickAdapter.data.get(i)?.let {
                    if (it is MessageItemData) {
                        when (view.id) {
                            R.id.message_content -> {
                                if (null != activity && null != it.mMessageInfoItem) {
                                    it.mMessageInfoItem?.setHasRead(true)
                                    showMessage(activity!!, it.mMessageInfoItem!!, false)
                                    notifyItemChanged(i)
                                    mDataLiveData.initData()
                                }
                            }
                            R.id.message_delete -> {
                                getMessageManager().deleteMessage(it.mMessageInfoItem)
                                mDataLiveData.initData()
                            }
                        }
                    }
                }
            }

            emptyView = PlaceholderView(context!!, ThemeResourcesManager.getString(ModelResR.string.com_bihe0832_message_empty_text))
        }
    }

    private val mCommonListLiveData = object : CommonListLiveData() {
        override fun initData() {
            val newData = getMessageManager().getMessageLiveData().value?.filter { !it.hasDelete() }?.map { MessageItemData(it) }
                    ?: mutableListOf()
            postValue(newData)
        }

        override fun refresh() {
            getMessageManager().updateMsg()
            initData()
        }

        override fun loadMore() {

        }

        override fun hasMore(): Boolean {
            return false
        }

        override fun canRefresh(): Boolean {
            return true
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mCommonListLiveData
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean, hasCreateView: Boolean) {
        super.setUserVisibleHint(isVisibleToUser, hasCreateView)
        if (isVisibleToUser && hasCreateView) {
            getMessageManager().updateMsg()
        }
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf(CardItemForCommonList(MessageItemData::class.java))
    }
}