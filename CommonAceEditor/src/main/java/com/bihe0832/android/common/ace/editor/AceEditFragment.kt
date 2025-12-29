/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/5/31 下午11:41
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/31 下午11:38
 *
 */

package com.bihe0832.android.common.ace.editor

import android.view.View
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.ace.editor.AceConstants
import com.bihe0832.android.lib.ace.editor.AceEditorView
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog
import com.bihe0832.android.model.res.R as ModelResR

class AceEditFragment : BaseFragment() {
    private var filePath = ""

    private var mLoadingDialog: LoadingDialog? = null

    override fun getLayoutID(): Int {
        return R.layout.fragment_ace_edit
    }

    fun setFilePath(filePath: String) {
        this.filePath = filePath
    }

    fun setReadOnly(readOnly: Boolean) {
        Config.writeConfig(AceConstants.KEY_LAST_IS_READ_ONLY, readOnly)
        view?.findViewById<AceEditorView>(R.id.main_ace_editor)?.isReadOnly = readOnly
    }

    fun isReadOnly(): Boolean {
        return view?.findViewById<AceEditorView>(R.id.main_ace_editor)?.isReadOnly ?: true
    }

    fun setAutoWrap(autoWrap: Boolean) {
        Config.writeConfig(AceConstants.KEY_LAST_IS_AUTO_WRAP, autoWrap)
        view?.findViewById<AceEditorView>(R.id.main_ace_editor)?.setWrap(autoWrap)
    }

    fun isAutoWrap(): Boolean {
        return view?.findViewById<AceEditorView>(R.id.main_ace_editor)?.isWrap ?: true
    }

    override fun initView(view: View) {
        super.initView(view)
        view?.findViewById<AceEditorView>(R.id.main_ace_editor)?.apply {
            isReadOnly =
                Config.isSwitchEnabled(AceConstants.KEY_LAST_IS_READ_ONLY, AceConstants.VALUE_LAST_IS_READ_ONLY)
            isWrap = Config.isSwitchEnabled(AceConstants.KEY_LAST_IS_AUTO_WRAP, AceConstants.VALUE_LAST_IS_AUTO_WRAP)
            textSize = Config.readConfig(AceConstants.KEY_LAST_TEXT_SIZE, AceConstants.VALUE_LAST_TEXT_SIZE)
            setDebug(!ZixieContext.isOfficial())
        }
        mLoadingDialog = LoadingDialog(context).apply {
            setIsFullScreen(true)
            setLoadingType(LoadingDialog.LOADING_TYPE_DOTS)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean, hasCreateView: Boolean) {
        super.setUserVisibleHint(isVisibleToUser, hasCreateView)
        if (hasCreateView && isVisibleToUser) {
            view?.findViewById<AceEditorView>(R.id.main_ace_editor)?.isReadOnly =
                Config.isSwitchEnabled(AceConstants.KEY_LAST_IS_READ_ONLY, AceConstants.VALUE_LAST_IS_READ_ONLY)
            view?.findViewById<AceEditorView>(R.id.main_ace_editor)?.isWrap =
                Config.isSwitchEnabled(AceConstants.KEY_LAST_IS_AUTO_WRAP, AceConstants.VALUE_LAST_IS_AUTO_WRAP)
        }
    }

    override fun initData() {
        super.initData()
        if (FileUtils.checkFileExist(filePath)) {
            mLoadingDialog?.show(ThemeResourcesManager.getString(ModelResR.string.ace_editor_load_file_tips))
            ThreadManager.getInstance().start {
                FileUtils.getFileBytes(filePath).let {
                    view?.findViewById<AceEditorView>(R.id.main_ace_editor)?.loadContent(filePath, it)
                    mLoadingDialog?.dismiss()
                }
            }
        }
    }
}
