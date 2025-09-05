package com.bihe0832.android.base.compose.debug.media

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.item.requestPermissionForDebug
import com.bihe0832.android.common.compose.debug.module.DebugCommonComposeFragment
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.crop.CropUtils
import com.bihe0832.android.common.crop.constants.CropConstants
import com.bihe0832.android.common.crop.view.OverlayView
import com.bihe0832.android.common.crop.wrapper.CropWrapper
import com.bihe0832.android.common.photos.choosePhoto
import com.bihe0832.android.common.photos.cropPhoto
import com.bihe0832.android.common.photos.getAutoChangedCropUri
import com.bihe0832.android.common.photos.getAutoChangedPhotoUri
import com.bihe0832.android.common.photos.takePhoto
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FILE_TYPE_ALL
import com.bihe0832.android.lib.file.mimetype.FILE_TYPE_IMAGE
import com.bihe0832.android.lib.file.mimetype.FILE_TYPE_VIDEO
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.Media
import java.io.File

class DebugPhotosFragment : DebugCommonComposeFragment() {
    val LOG_TAG = this.javaClass.simpleName

    var needCrop = false
    var needAAFCrop = false

    var takePhosUri: Uri? = null
    var cropUri: Uri? = null


    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                debugPhotoView()
            }
        }
    }

    @Composable
    fun debugPhotoView() {
        val activity = LocalContext.current as? Activity
        DebugContent {
            DebugTips("当前图片地址： ")
            activity?.let {
                DebugItem("调试中临时申请指定权限") {
                    requestPermissionForDebug(activity, listOf(Manifest.permission.CAMERA))
                }
            }

            DebugItem("AAF 裁剪") {
                needCrop = false
                needAAFCrop = true
                aafcrop()
            }
            DebugItem("仅拍照") {
                needCrop = false
                needAAFCrop = false
                takePhosUri = activity!!.getAutoChangedPhotoUri()
                takePhoto(takePhosUri)
            }
            DebugItem("拍照并系统裁剪") {
                needCrop = true
                needAAFCrop = false
                takePhosUri = activity!!.getAutoChangedPhotoUri()
                takePhoto(takePhosUri)
            }
            DebugItem("拍照并AAF裁剪") {
                needCrop = false
                needAAFCrop = true
                takePhosUri = activity!!.getAutoChangedPhotoUri()
                takePhoto(takePhosUri)
            }
            DebugItem("选择图片") {
                needCrop = false
                needAAFCrop = false
                choosePhoto(FILE_TYPE_IMAGE)
            }
            DebugItem("选择视频") {
                needCrop = false
                needAAFCrop = false
                choosePhoto(FILE_TYPE_VIDEO)
            }
            DebugItem("选择图片或者视频") {
                needCrop = false
                needAAFCrop = false
                choosePhoto(FILE_TYPE_ALL)
            }
            DebugItem("选择并系统裁剪") {
                needCrop = true
                needAAFCrop = false
                choosePhoto()
            }
            DebugItem("选择并AAF裁剪") {
                needCrop = false
                needAAFCrop = true
                choosePhoto()
            }
            DebugItem("AAF裁剪并通过回调返回") {
                testCropCallBack()
            }
        }
    }

    private fun testCropCallBack() {
        val sourceFile = AAFFileWrapper.getTempFolder() + "cv_v.jpg"
        FileUtils.copyAssetsFileToPath(context, "cv_v.jpg", sourceFile)
        CropWrapper.startCrop(ZixieFileProvider.getZixieFileProvider(context!!, File(sourceFile)),
            CropUtils.Options().apply {
                setHideBottomControls(true)
                withAspectRatio(3f, 2f)
                setCircleDimmedLayer(true)
            },
            object : AAFDataCallback<Uri>() {
                override fun onSuccess(result: Uri?) {
                    ZLog.d(result.toString())
                }
            })
    }

    private fun aafcrop() {
        val sourceFile = AAFFileWrapper.getTempFolder() + "cv_v.jpg"
        FileUtils.copyAssetsFileToPath(context, "cv_v.jpg", sourceFile)
        CropUtils.startCrop(
            this,
            ZixieFileProvider.getZixieFileProvider(context!!, File(sourceFile)),
            CropUtils.Options().apply {
//                setHideBottomControls(true)
                withAspectRatio(3f, 2f)
                setCircleDimmedLayer(true)
            },
        )
    }

    private fun cropPhotos(sourceUri: Uri?) {
        cropUri = activity!!.getAutoChangedCropUri()
        cropPhoto(sourceUri, cropUri, 2, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK == resultCode) {
            ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode")
            when (requestCode) {
                ZixieActivityRequestCode.TAKE_PHOTO -> {
                    if (needCrop) {
                        cropPhotos(takePhosUri)
                    } else if (needAAFCrop) {
                        CropUtils.startCrop(this, takePhosUri)
                    } else {
                        ZLog.d("图片地址:" + Media.uriToFile(context!!, takePhosUri))
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
                    } else if (needAAFCrop) {
                        CropUtils.startCrop(this, data.getData(), CropUtils.Options().apply {
                            setAllowedGestures(
                                CropConstants.GESTURE_TYPES_SCALE,
                                CropConstants.GESTURE_TYPES_ROTATE,
                                CropConstants.GESTURE_TYPES_SCALE
                            )
                            setFreeStyleCropType(OverlayView.FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH)
                        })
                    } else {
                        ZLog.d(
                            "图片地址:" + Media.uriToFile(
                                activity!!, data.getData()
                            ).absolutePath
                        )
                    }
                } else {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode")
                }

                ZixieActivityRequestCode.CROP_PHOTO -> {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：" + requestCode + "；resultCode：" + data.toString())
                    if (needCrop) {
                        ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：" + requestCode + "；resultCode：" + data.toString())
                        Media.uriToFile(activity!!, cropUri).absolutePath.let {
                            ZLog.d("图片地址:$it")
                            ZLog.d("PhotoChooser in cropUri：$it")
                        }
                    } else if (needAAFCrop) {
                        ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：" + requestCode + "；resultCode：" + data.toString())
                        ZLog.d("图片地址:${data?.data}")
                        Media.addToPhotos(
                            context!!, Media.uriToFile(context, data?.data).absolutePath
                        )
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
