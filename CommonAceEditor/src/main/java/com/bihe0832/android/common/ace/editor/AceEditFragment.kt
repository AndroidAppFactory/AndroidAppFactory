/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/5/31 下午11:41
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/31 下午11:38
 *
 */

package com.bihe0832.android.common.ace.editor

import android.view.View
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.LoadingDialog
import kotlinx.android.synthetic.main.fragment_ace_edit.*


class AceEditFragment : BaseFragment() {
    private var filePath = ""
    private val mLoadingDialog by lazy {
        LoadingDialog(context).apply {
            setIsFullScreen(true)
            setLoadingType(LoadingDialog.LOADING_TYPE_DOTS)
        }
    }

    override fun getLayoutID(): Int {
        return R.layout.fragment_ace_edit
    }

    fun setFilePath(filePath: String) {
        this.filePath = filePath
    }


    override fun initView(view: View) {
        super.initView(view)
        main_ace_editor?.setReadOnly(true)
    }

    override fun initData() {
        super.initData()
        if (FileUtils.checkFileExist(filePath)) {
            mLoadingDialog.show("文件加载中，请稍候")
            ThreadManager.getInstance().start {
                FileUtils.getFileBytes(filePath).let {
                    main_ace_editor?.loadContent(filePath, it)
                    mLoadingDialog.dismiss()
                }
            }

        } else {
            activity?.finish()
        }
    }
}