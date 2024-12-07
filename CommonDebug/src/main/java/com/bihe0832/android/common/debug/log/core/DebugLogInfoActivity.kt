package com.bihe0832.android.common.debug.log.core;

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListActivity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.text.ClipboardUtil
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.ui.menu.PopMenu
import com.bihe0832.android.lib.ui.menu.PopMenuItem
import com.bihe0832.android.lib.utils.ConvertUtils


open class DebugLogInfoActivity : CommonListActivity() {

    companion object {
        private val SORT = "aaf.debug.log.core.sort"
        private val NUM = "aaf.debug.log.core.num"
        private val SHOW_LINE = "aaf.debug.log.core.line"

        fun showLog(
            context: Context, filePath: String, sort: Boolean, showLine: Boolean, showNum: Int
        ) {
            val intent = Intent(context, DebugLogInfoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, filePath)
            intent.putExtra(SORT, sort)
            intent.putExtra(NUM, showNum)
            intent.putExtra(SHOW_LINE, showLine)
            context.startActivity(intent)
        }

        fun showLog(context: Context, logFileName: String, sort: Boolean, showLine: Boolean) {
            showLog(context, logFileName, sort, showLine, 2000)
        }
    }

    private var logPath = ""
    private var isSort = false
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
        return "日志功能汇总"
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mLiveData
    }

    override fun getEmptyText(): String {
        return "日志内容为空"
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(DebugItemData::class.java, true))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun parseBundle(bundle: Bundle) {
        super.parseBundle(bundle)
        logPath = bundle.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")
        if (logPath.isBlank() || !FileUtils.checkFileExist(logPath)) {
            ZixieContext.showToast("日志路径异常或日志不存在")
            finish()
        }
        findViewById<TextView>(R.id.title_text).apply {
            text = "查看日志：${FileUtils.getFileName(logPath)}"
        }
        isSort = bundle.getBoolean(SORT, false)
        showLine = bundle.getBoolean(SHOW_LINE, true)
        showNum = ConvertUtils.parseInt(bundle.getString(NUM, ""), 2000)
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
                PopMenu(this@DebugLogInfoActivity, this).apply {
                    ArrayList<PopMenuItem>().apply {
                        add(PopMenuItem().apply {
                            actionName = "发送日志"
                            iconResId = R.drawable.icon_send
                            setItemClickListener {
                                hide()
                                FileUtils.sendFile(this@DebugLogInfoActivity, logPath)
                            }
                        })
                        add(PopMenuItem().apply {
                            if (isSort) {
                                "正序"
                            } else {
                                "倒序"
                            }.let {
                                actionName = "当前内容$it"
                            }
                            iconResId = if (isSort) {
                                R.drawable.icon_ascending
                            } else {
                                R.drawable.icon_descending
                            }
                            setItemClickListener {
                                hide()
                                isSort = !isSort
                                mLiveData.postValue(mLiveData.value?.reversed())
                            }
                        })
                        add(PopMenuItem().apply {
                            if (showLine) {
                                "隐藏"
                            } else {
                                "展示"
                            }.let {
                                actionName = "${it}分隔线"
                            }
                            iconResId = R.drawable.icon_edit
                            setItemClickListener {
                                hide()
                                showLine = !showLine
                                mLiveData.refresh()
                            }
                        })
                        add(PopMenuItem().apply {
                            actionName = "调整展示行数"
                            iconResId = R.drawable.icon_number
                            setItemClickListener {
                                hide()
                                DialogUtils.showInputDialog(
                                    this@DebugLogInfoActivity,
                                    "调整日志行数",
                                    "请在下方输入你想展示的日志行数，后点击确定，当前行数：$showNum",
                                    showNum.toString()
                                ) { p0 ->
                                    if (!showNum.toString().equals(p0)) {
                                        showNum = ConvertUtils.parseInt(p0, showNum)
                                        mLiveData.refresh()
                                    }
                                }
                            }
                        })
                    }.let {
                        setMenuItemList(it)
                    }
                }.show()
            }
        }
    }

    fun getLogInfo() {
        val dataList = mutableListOf<CardBaseModule>()
        if (fileContentList.isNotEmpty()) {
            val logList = if (isSort) {
                fileContentList.reversed().take(showNum)
            } else {
                fileContentList.take(showNum)
            }
            logList.forEachIndexed { index, logInfo ->
                dataList.add(
                    DebugItemData(
                        logInfo,
                        null,
                        {
                            ClipboardUtil.copyToClipboard(this@DebugLogInfoActivity, logInfo)
                            ZixieContext.showToast("日志内容已经复制到剪切板")
                            true
                        },
                        10,
                        Color.parseColor("#333333"),
                        false,
                        false,
                        null,
                        6,
                        6,
                        if (index % 2 == 0) {
                            Color.parseColor("#FFFFFF")
                        } else {
                            Color.parseColor("#EEEEEE")
                        },
                        showLine
                    )
                )
            }
        }
        ThreadManager.getInstance().runOnUIThread {
            mLiveData.postValue(dataList)
        }
    }
}