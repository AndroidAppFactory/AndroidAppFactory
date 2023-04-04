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
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.ui.menu.PopMenu
import com.bihe0832.android.lib.ui.menu.PopMenuItem
import kotlinx.android.synthetic.main.activity_edit_layout.*
import java.net.URLDecoder


@Module(RouterConstants.MODULE_NAME_EDITOR)
class AceEditActivity : BaseActivity() {
    private var filePath = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_layout)
        initToolbar(R.id.common_toolbar, "文本查看器", true)
        parseBundle(intent.extras)
        initView()
    }

    private fun initView() {
        edit_menu_more.apply {
            setColorFilter(resources.getColor(R.color.white))
            setOnClickListener {
                PopMenu(this@AceEditActivity, edit_menu_more).apply {
                    ArrayList<PopMenuItem>().apply {
                        add(PopMenuItem().apply {
                            actionName = getString(R.string.ace_editor_menu_open_new)
                            iconResId = R.mipmap.ic_folder_open
                            setItemClickListener {
                                hide()
                                FileSelectTools.openFileSelect(this@AceEditActivity, ZixieContext.getLogFolder())
                            }
                        })
                        add(PopMenuItem().apply {
                            actionName = getString(R.string.ace_editor_menu_share)
                            iconResId = R.mipmap.ic_share
                            setItemClickListener {
                                hide()
                                FileUtils.sendFile(this@AceEditActivity, filePath)
                            }
                        })

                        add(PopMenuItem().apply {
                            if (mAceEditFragment.isAutoWrap()) {
                                R.string.ace_editor_menu_close
                            } else {
                                R.string.ace_editor_menu_open
                            }.let {
                                actionName = getString(it, getString(R.string.ace_editor_menu_auto_wrap))
                            }

                            iconResId = R.mipmap.ic_wrap_text
                            setItemClickListener {
                                hide()
                                mAceEditFragment.setAutoWrap(!mAceEditFragment.isAutoWrap())
                            }
                        })

//                        add(PopMenuItem().apply {
//                            if (mAceEditFragment.isReadOnly()) {
//                                R.string.ace_editor_menu_open
//                            } else {
//                                R.string.ace_editor_menu_close
//                            }.let {
//                                actionName = getString(it, getString(R.string.ace_editor_menu_edit))
//                            }
//
//                            iconResId = R.mipmap.ic_edit
//                            setItemClickListener {
//                                hide()
//                                mAceEditFragment.setReadOnly(!mAceEditFragment.isReadOnly())
//                            }
//                        })
                    }.let {
                        setMenuItemList(it)
                    }
                }.let {
                    it.show()
                }
            }
        }
    }

    private val mAceEditFragment by lazy {
        AceEditFragment()
    }

    override fun onResume() {
        super.onResume()
        if (FileUtils.checkFileExist(filePath)) {
            if (FileMimeTypes.isTextFile(filePath)) {
                showBadFile(getString(R.string.ace_editor_load_file_not_found))
            } else {
                mAceEditFragment.setFilePath(filePath)
                if (findFragment(AceEditFragment::class.java) == null) {
                    loadRootFragment(R.id.common_fragment_content, mAceEditFragment)
                }
            }
        } else {
            if (TextUtils.isEmpty(filePath)) {
                showBadFile(getString(R.string.ace_editor_load_file_empty))
            } else {
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
            ZLog.d(RouterConstants.MODULE_NAME_EDITOR, "filepath:$filePath")
            updateTitle(FileUtils.getFileName(filePath))
        }
    }


    private fun showBadFile(msg: String) {
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