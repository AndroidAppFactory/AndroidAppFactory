package com.bihe0832.android.common.file.preview;

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListActivity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.text.ClipboardUtil
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.ui.menu.PopMenu
import com.bihe0832.android.lib.ui.menu.PopMenuItem
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.model.res.R as ModelResR
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.framework.R as FrameworkR


@Module(RouterConstants.MODULE_NAME_SHOW_FILE_CONTENT)
open class FileContentInfoActivity : CommonListActivity() {

    private var logPath = ""
    private var isReversed = false
    private var showLine = true
    private var showNum = 2000
    private var fileContentList = mutableListOf<String>()
    private var mLiveData = object : CommonListLiveData() {
        override fun initData() {
            getLogInfo()
        }

        override fun refresh() {
            getLogInfo()
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

    override fun getTitleText(): String {
        return getString(ModelResR.string.common_file_preview_title)
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mLiveData
    }

    override fun getEmptyText(): String {
        return getString(ModelResR.string.common_file_preview_empty)
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(ContentItemData::class.java, true))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun parseBundle(bundle: Bundle) {
        super.parseBundle(bundle)
        logPath = bundle.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")
        if (logPath.isBlank() || !FileUtils.checkFileExist(logPath)) {
            ZixieContext.showToast(getString(ModelResR.string.ace_editor_load_file_not_found))
            finish()
        }
        findViewById<TextView>(R.id.title_text).apply {
            text = FileUtils.getFileName(logPath)
        }
        isReversed = ConvertUtils.parseBoolean(
            bundle.getString(
                RouterConstants.INTENT_EXTRA_KEY_SHOW_LOG_SORT,
                ""
            ), false
        )
        showLine = ConvertUtils.parseBoolean(
            bundle.getString(
                RouterConstants.INTENT_EXTRA_KEY_SHOW_LOG_SHOW_LINE,
                ""
            ), true
        )
        showNum = ConvertUtils.parseInt(
            bundle.getString(
                RouterConstants.INTENT_EXTRA_KEY_SHOW_LOG_NUM,
                ""
            ), 2000
        )
        ThreadManager.getInstance().start {
            FileUtils.getFileContent(logPath).split("\n").let {
                fileContentList.clear()
                fileContentList.addAll(it)
            }
            if (fileContentList.isNotEmpty()) {
                mLiveData.refresh()
            }

        }
    }

    override fun getResID(): Int {
        return R.layout.com_bihe0832_activity_debug_core_log
    }

    override fun initToolbar(resID: Int, titleString: String?, needBack: Boolean) {
        findViewById<View>(R.id.navigation_back).setOnClickListener {
            onBackPressedSupport()
        }
        findViewById<View>(R.id.title_icon).apply {
            setOnClickListener {
                PopMenu(this@FileContentInfoActivity, this).apply {
                    setMenuItemList(getPopMenuItem(this))
                }.show()
            }
        }
    }

    fun getSendLogMenu(menu: PopMenu): PopMenuItem {
        return PopMenuItem().apply {
            actionName = getString(ModelResR.string.common_file_menu_share)
            iconResId = ResR.drawable.icon_send
            setItemClickListener {
                menu.hide()
                FileUtils.sendFile(this@FileContentInfoActivity, logPath)
            }
        }
    }

    fun getReversedMenu(menu: PopMenu): PopMenuItem {
        return PopMenuItem().apply {
            actionName = if (isReversed) {
                getString(ModelResR.string.common_file_menu_show_sort)
            } else {
                getString(ModelResR.string.common_file_menu_show_reversed)
            }
            iconResId = if (isReversed) {
                FrameworkR.drawable.icon_ascending
            } else {
                FrameworkR.drawable.icon_descending
            }
            setItemClickListener {
                menu.hide()
                isReversed = !isReversed
                mLiveData.postValue(mLiveData.value?.reversed())
            }
        }
    }

    fun getShowLineMenu(menu: PopMenu): PopMenuItem {
        return PopMenuItem().apply {
            actionName = if (showLine) {
                getString(ModelResR.string.common_file_menu_driver_hide)
            } else {
                getString(ModelResR.string.common_file_menu_driver_show)
            }
            iconResId = FrameworkR.drawable.icon_edit
            setItemClickListener {
                menu.hide()
                showLine = !showLine
                mLiveData.refresh()
            }
        }
    }

    fun getChangeNumMenu(menu: PopMenu): PopMenuItem {
        return PopMenuItem().apply {
            actionName = getString(ModelResR.string.common_file_line_title)
            iconResId = FrameworkR.drawable.icon_number
            setItemClickListener {
                menu.hide()
                DialogUtils.showInputDialog(
                    this@FileContentInfoActivity,
                    getString(ModelResR.string.common_file_line_title),
                    String.format(getString(ModelResR.string.common_file_line_desc), showNum.toString()),
                    showNum.toString()
                ) { p0 ->
                    if (!showNum.toString().equals(p0)) {
                        showNum = ConvertUtils.parseInt(p0, showNum)
                        mLiveData.refresh()
                    }
                }
            }
        }
    }

    open fun getPopMenuItem(menu: PopMenu): ArrayList<PopMenuItem> {
        return ArrayList<PopMenuItem>().apply {
            add(getSendLogMenu(menu))
            add(getReversedMenu(menu))
            add(getShowLineMenu(menu))
            add(getChangeNumMenu(menu))
        }
    }

    fun getLogInfo() {
        val dataList = mutableListOf<CardBaseModule>()
        if (fileContentList.isNotEmpty()) {
            val logList = if (isReversed) {
                fileContentList.reversed().take(showNum)
            } else {
                fileContentList.take(showNum)
            }
            logList.forEachIndexed { index, logInfo ->
                dataList.add(getLineItem(index, logInfo))
            }
        }
        ThreadManager.getInstance().runOnUIThread {
            mLiveData.postValue(dataList)
        }
    }

    open fun getLineItem(index: Int, logInfo: String): ContentItemData {
        return ContentItemData(
            logInfo, null, {
                ClipboardUtil.copyToClipboard(this@FileContentInfoActivity, logInfo)
                ZixieContext.showToast(getString(ModelResR.string.common_file_copy))
                true
            }, 10, Color.parseColor("#333333"), false, false, null, 6, 6, if (index % 2 == 0) {
                Color.parseColor("#FFFFFF")
            } else {
                Color.parseColor("#EEEEEE")
            }, if (showLine) {
                Color.parseColor("#333333")
            } else {
                Color.TRANSPARENT
            }
        )
    }
}