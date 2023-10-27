package com.bihe0832.android.base.debug.photos

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.common.debug.module.DebugCommonFragment
import com.bihe0832.android.common.photos.choosePhoto
import com.bihe0832.android.common.photos.cropPhoto
import com.bihe0832.android.common.photos.getAutoChangedCropUri
import com.bihe0832.android.common.photos.getAutoChangedPhotoUri
import com.bihe0832.android.common.photos.takePhoto
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.mimetype.FILE_TYPE_ALL
import com.bihe0832.android.lib.file.mimetype.FILE_TYPE_IMAGE
import com.bihe0832.android.lib.file.mimetype.FILE_TYPE_VIDEO
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.Media

class DebugPhotosFragment : DebugCommonFragment() {
    val LOG_TAG = this.javaClass.simpleName

    var needCrop = false

    var takePhosUri: Uri? = null
    var cropUri: Uri? = null

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugTipsData("当前图片地址： "))
            add(
                DebugItemData(
                    "调试中临时申请指定权限",
                    View.OnClickListener {
                        requestPermissionForDebug(
                            listOf(
                                Manifest.permission.CAMERA,
                            ),
                        )
                    },
                ),
            )
            add(
                DebugItemData(
                    "仅拍照",
                    View.OnClickListener {
                        needCrop = false
                        takePhosUri = activity!!.getAutoChangedPhotoUri()
                        activity?.takePhoto(takePhosUri)
                    },
                ),
            )
            add(
                DebugItemData(
                    "拍照并裁剪",
                    View.OnClickListener {
                        needCrop = true
                        takePhosUri = activity!!.getAutoChangedPhotoUri()
                        activity?.takePhoto(takePhosUri)
                    },
                ),
            )
            add(
                DebugItemData(
                    "选择图片",
                    View.OnClickListener {
                        needCrop = false
                        activity?.choosePhoto(FILE_TYPE_IMAGE)
                    },
                ),
            )
            add(
                DebugItemData(
                    "选择视频",
                    View.OnClickListener {
                        needCrop = false
                        activity?.choosePhoto(FILE_TYPE_VIDEO)
                    },
                ),
            )
            add(
                DebugItemData(
                    "选择图片或者视频",
                    View.OnClickListener {
                        needCrop = false
                        activity?.choosePhoto(FILE_TYPE_ALL)
                    },
                ),
            )
            add(
                DebugItemData(
                    "选择并裁剪",
                    View.OnClickListener {
                        needCrop = true
                        activity?.choosePhoto()
                    },
                ),
            )
        }
    }

    private fun cropPhotos(sourceUri: Uri?) {
        cropUri = activity!!.getAutoChangedCropUri()
        activity!!.cropPhoto(sourceUri, cropUri, 2, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK == resultCode) {
            ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode")
            when (requestCode) {
                ZixieActivityRequestCode.TAKE_PHOTO -> {
                    if (needCrop) {
                        cropPhotos(takePhosUri)
                    } else {
                        showResult("图片地址:" + Media.uriToFile(context!!, takePhosUri))
//                        showResult("图片地址:" + ZixieFileProvider.uriToFile(activity!!, takePhosUri).absolutePath)
                    }
                }

                ZixieActivityRequestCode.CHOOSE_PHOTO -> if (data != null && data.data != null) {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode $data")
                    if (needCrop) {
                        cropPhotos(
                            ZixieFileProvider.getZixieFileProvider(
                                context,
                                Media.uriToFile(activity!!, data.getData()),
                            ),
                        )
                    } else {
                        showResult("图片地址:" + Media.uriToFile(activity!!, data.getData()).absolutePath)
                    }
                } else {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode")
                }

                ZixieActivityRequestCode.CROP_PHOTO -> {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：" + requestCode + "；resultCode：" + data.toString())
                    Media.uriToFile(activity!!, cropUri).absolutePath.let {
                        showResult("图片地址:$it")
                        ZLog.d("PhotoChooser in cropUri：$it")
                    }
                }

                else -> {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：" + requestCode + "；resultCode：" + data.toString())
                }
            }
        } else {
            ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode")
        }
    }
}
