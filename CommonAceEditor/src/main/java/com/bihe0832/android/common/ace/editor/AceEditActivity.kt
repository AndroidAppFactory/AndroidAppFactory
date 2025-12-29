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
import android.widget.ImageView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.ui.menu.PopMenu
import com.bihe0832.android.lib.ui.menu.PopMenuItem
import com.bihe0832.android.model.res.R as ModelResR
import com.bihe0832.android.framework.R as FrameworkR
import com.bihe0832.android.lib.aaf.res.R as ResR
import java.net.URLDecoder


@Module(RouterConstants.MODULE_NAME_EDITOR)
class AceEditActivity : BaseActivity() {
    private var filePath = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_layout)
        initToolbar(R.id.common_toolbar, resources.getString(ModelResR.string.ace_editor_title), true)
        parseBundle(intent.extras)
        initView()
    }

    private fun initView() {
        findViewById<ImageView>(R.id.edit_menu_more).apply {
            setOnClickListener {
                PopMenu(this@AceEditActivity, this).apply {
                    ArrayList<PopMenuItem>().apply {
                        add(PopMenuItem().apply {
                            actionName =
                                ThemeResourcesManager.getString(ModelResR.string.common_file_menu_open_new)
                            iconResId = FrameworkR.drawable.icon_folder_open
                            setItemClickListener {
                                hide()
                                FileSelectTools.openFileSelect(
                                    this@AceEditActivity,
                                    ZixieContext.getLogFolder()
                                )
                            }
                        })
                        add(PopMenuItem().apply {
                            actionName =
                                ThemeResourcesManager.getString(ModelResR.string.common_file_menu_share)
                            iconResId = ResR.drawable.icon_send
                            setItemClickListener {
                                hide()
                                FileUtils.sendFile(this@AceEditActivity, filePath)
                            }
                        })

                        add(PopMenuItem().apply {
                            if (mAceEditFragment.isAutoWrap()) {
                                ModelResR.string.common_file_menu_close
                            } else {
                                ModelResR.string.common_file_menu_open
                            }.let {
                                actionName =
                                    ThemeResourcesManager.getString(it) + ThemeResourcesManager.getString(
                                        ModelResR.string.common_file_menu_auto_wrap
                                    )
                            }

                            iconResId = FrameworkR.drawable.icon_wrap_text
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
            if (!FileMimeTypes.isTextFile(filePath)) {
                showBadFile(ThemeResourcesManager.getString(ModelResR.string.ace_editor_load_file_folder)!!)
            } else {
                mAceEditFragment.setFilePath(filePath)
                if (findFragment(AceEditFragment::class.java) == null) {
                    loadRootFragment(R.id.common_fragment_content, mAceEditFragment)
                }
            }
        } else {
            if (TextUtils.isEmpty(filePath)) {
                showBadFile(ThemeResourcesManager.getString(ModelResR.string.ace_editor_load_file_empty)!!)
            } else {
                showBadFile(ThemeResourcesManager.getString(ModelResR.string.ace_editor_load_file_not_found)!!)
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
            ThemeResourcesManager.getString(ModelResR.string.dialog_title)!!,
            msg,
            resources.getString(ModelResR.string.ace_editor_load_file_not_found_positive),
            resources.getString(ModelResR.string.ace_editor_load_file_not_found_negative),
            false,
            object : OnDialogListener {
                override fun onPositiveClick() {
                    FileSelectTools.openFileSelect(
                        this@AceEditActivity,
                        ZixieContext.getLogFolder()
                    )
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