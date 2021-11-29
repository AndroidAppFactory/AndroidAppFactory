package com.bihe0832.android.base.test.photos


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import com.bihe0832.android.common.photos.*
import com.bihe0832.android.common.test.base.BaseTestListFragment
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import java.io.File


class TestPhotosFragment : BaseTestListFragment() {
    val LOG_TAG = "Test"

    var needCrop = false

    private fun getFile(): File {
        return File(activity!!.getPhotosFolder() + System.currentTimeMillis() + "_crop_mna_hippy.jpg")
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("拍照", View.OnClickListener { activity?.takePhoto(getFile()) }))
            add(TestItemData("选择图片", View.OnClickListener {
                needCrop = false
                activity?.choosePhoto()
            }))
            add(TestItemData("选择并裁剪", View.OnClickListener {
                needCrop = true
                activity?.choosePhoto()
            }))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK == resultCode) {
            ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode")
            when (requestCode) {
                ZixieActivityRequestCode.TAKE_PHOTO -> activity!!.cropPhoto(
                    activity!!.getDefaultPhoto().absolutePath,
                    getFile().absolutePath
                )
                ZixieActivityRequestCode.CHOOSE_PHOTO -> if (data != null && data.data != null) {
                    if (needCrop) {
                        activity?.cropPhoto(data.getData(), getFile().absolutePath)
                    }
                } else {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode")
                }
                ZixieActivityRequestCode.CROP_PHOTO -> {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：" + requestCode + "；resultCode：" + data.toString())
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