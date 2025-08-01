package com.bihe0832.android.base.debug.panel

import android.content.res.Configuration
import android.view.View
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.panel.PanelUtils
import com.bihe0832.android.common.panel.data.PanelStorageManager
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.panel.PanelManager
import com.bihe0832.android.lib.panel.constants.DrawEvent
import com.bihe0832.android.lib.panel.event.DrawEventLiveData

open class DebugPanelFragment : DebugEnvFragment() {

    private var filePath = PanelStorageManager.getPanelSavePath("Temp")
    override fun initView(view: View) {
        super.initView(view)
        DrawEventLiveData.observe(this) { event: Int ->
            if (event == DrawEvent.STATUS_SAVED) {
                filePath = PanelManager.getInstance().mFilePath
            }
        }
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(
                getDebugItem(
                    "新建横屏",
                    View.OnClickListener {
                        PanelUtils.startPanel(context, Configuration.ORIENTATION_LANDSCAPE)
                    },
                ),
            )
            add(
                getDebugItem(
                    "新建竖屏",
                    View.OnClickListener {
                        PanelUtils.startPanel(context, Configuration.ORIENTATION_PORTRAIT)
                    },
                ),
            )
            add(
                getDebugItem(
                    "新建指定文件名横屏",
                    View.OnClickListener {
                        PanelUtils.startPanel(context, filePath, Configuration.ORIENTATION_LANDSCAPE)
                    },
                ),
            )
            add(
                getDebugItem(
                    "新建指定文件名竖屏",
                    View.OnClickListener {
                        PanelUtils.startPanel(context, filePath, Configuration.ORIENTATION_PORTRAIT)
                    },
                ),
            )
            add(
                getDebugItem(
                    "读取已存在",
                    View.OnClickListener {
                        PanelUtils.loadPanelByPath(context, filePath)
                    },
                ),
            )
        }
    }
}
