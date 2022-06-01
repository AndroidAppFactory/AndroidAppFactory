/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/5/31 下午11:41
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/31 下午11:37
 *
 */

package com.bihe0832.android.common.ace.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import java.net.URLDecoder

@Module(RouterConstants.MODULE_NAME_EDITOR)
class AceEditActivity : CommonActivity() {
    private var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar("文件查看器", true)
        parseBundle(intent.extras)
    }

    private val mAceEditFragment by lazy {
        AceEditFragment()
    }

    override fun onResume() {
        super.onResume()
        if (FileUtils.checkFileExist(filePath)) {
            mAceEditFragment.setFilePath(filePath)
            if (findFragment(AceEditFragment::class.java) == null) {
                loadRootFragment(R.id.common_fragment_content, mAceEditFragment)
            }
        } else {
            if (TextUtils.isEmpty(filePath)){
                showBadFile(getString(R.string.ace_editor_load_file_not_found))
            }else{
                showBadFile(getString(R.string.ace_editor_load_file_not_found))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ZixieActivityRequestCode.FILE_CHOOSER) {
            data?.extras?.let {
                parseBundle(it)
            }
        }
    }

    private fun parseBundle(bundle: Bundle?) {
        bundle?.getString(RouterConstants.INTENT_EXTRA_KEY_WEB_URL)?.let {
            filePath = URLDecoder.decode(it)
            initToolbar(FileUtils.getFileName(filePath), true)

        }
    }


    private fun showBadFile(msg:String) {
        DialogUtils.showConfirmDialog(
                this,
                getString(R.string.dialog_title),
                msg,
                "选择文件",
                "退出查看",
                false,
                object : OnDialogListener {
                    override fun onPositiveClick() {
                        FileSelectTools.openFileSelect(this@AceEditActivity, ZixieContext.getLogFolder())
                    }

                    override fun onNegativeClick() {
                        finish()
                    }

                    override fun onCancel() {
                        finish()
                    }

                })
    }

}