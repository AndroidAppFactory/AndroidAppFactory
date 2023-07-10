package com.bihe0832.android.common.photos

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.permission.AAFPermissionManager
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.OSUtils
import kotlinx.android.synthetic.main.com_bihe0832_dialog_photo_chooser.view.*
import java.io.File


const val GOOGLE_PHOTO_PREFIX = "content://com.google.android.apps.photos.contentprovider"


fun getAutoChangedPhotoName(): String {
    return "zixie_" + System.currentTimeMillis() + ".jpg"
}

fun Activity.getPhotosFolder(): String {
    return Media.getZixiePhotosPath(this)
}

fun Activity.getAutoChangedPhotoUri(): Uri? {
    return getPhotosUri(getAutoChangedPhotoName())
}

fun Activity.getAutoChangedCropUri(): Uri? {
    return getCropUri(getAutoChangedPhotoName())
}

fun Activity.getCropUri(fileName: String): Uri? {
    return if (OSUtils.isAndroidQVersion()) {
        Media.createImageUriAboveAndroidQ(
                this,
                "",
                fileName
        )
    } else {
        Media.createImageUriForCropBelowAndroidQ(
                this,
                "",
                fileName
        )
    }
}

fun Activity.getPhotosUri(fileName: String): Uri? {
    return if (OSUtils.isAndroidQVersion()) {
        Media.createImageUriAboveAndroidQ(
                this,
                "",
                fileName
        )
    } else {
        Media.createImageUriForCameraBelowAndroidQ(
                this,
                "",
                fileName
        )
    }
}

/**
 * TargetFile 建议使用 [getPhotosFolder] 获取
 */
fun Activity.cropPhoto(sourceFile: String, targetFile: Uri?, aspectX: Int = 1, aspectY: Int = 1) {
    var sourceFileProvider =
            ZixieFileProvider.getZixieFileProvider(this, File(sourceFile))
    cropPhoto(sourceFileProvider, targetFile, aspectX, aspectY)
}


fun Activity.cropPhoto(sourceFile: Intent?, targetFile: Uri?, aspectX: Int = 1, aspectY: Int = 1) {
    var file = ZixieFileProvider.uriToFile(this, sourceFile?.data)
    cropPhoto(ZixieFileProvider.getZixieFileProvider(this, file), targetFile, aspectX, aspectY)
}

fun Activity.cropPhoto(
        sourceFile: Uri?,
        targetFile: Uri?,
        aspectX: Int = 1,
        aspectY: Int = 1
) {
    ZLog.d("Activity cropPhoto sourceFile ：$sourceFile")
    ZLog.d("Activity cropPhoto targetFile ：$targetFile")

    var finalSourceFile: Uri? = if (sourceFile.toString().startsWith(GOOGLE_PHOTO_PREFIX, true)) {
        ZixieFileProvider.getZixieFileProvider(this, ZixieFileProvider.uriToFile(this, sourceFile))
    } else {
        sourceFile
    }
    val file = ZixieFileProvider.uriToFile(this, targetFile)
    if (file != null) {
        FileUtils.checkAndCreateFolder(file.parent)
    }

    var outSizePerPart = 1080 / aspectX.coerceAtLeast(aspectY)
    // 开始切割
    val intent = Intent("com.android.camera.action.CROP").apply {
        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setDataAndType(finalSourceFile, "image/*")
        putExtra("crop", "true")
        putExtra("aspectX", aspectX) // 裁剪框比例
        putExtra("aspectY", aspectY)
        putExtra("outputX", outSizePerPart * aspectX) // 输出图片大小
        putExtra("outputY", outSizePerPart * aspectY)
        putExtra("scale", true)
        putExtra("scaleUpIfNeeded", true)
        putExtra("circleCrop", true)
        putExtra("return-data", false) // 不直接返回数据
        putExtra(MediaStore.EXTRA_OUTPUT, targetFile) // 返回一个文件
        putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
    }
    startActivityForResult(intent, ZixieActivityRequestCode.CROP_PHOTO)
}

fun Activity.takePhoto(outputUri: Uri?) {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    startActivityForResult(intent, ZixieActivityRequestCode.TAKE_PHOTO)
}

fun Activity.choosePhoto() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
    try {
        startActivityForResult(intent, ZixieActivityRequestCode.CHOOSE_PHOTO)
    } catch (e: Exception) {
        e.printStackTrace()
        ZixieContext.showDebug("未找到图片查看器")
    }
}

fun Activity.getPhotoContent() {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
    try {
        startActivityForResult(intent, ZixieActivityRequestCode.CHOOSE_PHOTO)
    } catch (e: Exception) {
        e.printStackTrace()
        ZixieContext.showDebug("未找到图片查看器")
    }
}

fun Activity.showPhotoChooser() {
    val view = LayoutInflater.from(this).inflate(R.layout.com_bihe0832_dialog_photo_chooser, null)
    val dialog = AlertDialog.Builder(this).setView(view).create()
    dialog.setCanceledOnTouchOutside(true)
    dialog.show()

    view.takePhotoBtn.setOnClickListener {
        PermissionManager.checkPermission(
                this,
                "PhotoSelect",
                false,
                object : PermissionManager.OnPermissionResult {
                    override fun onFailed(msg: String) {
                        dialog.dismiss()
                    }

                    override fun onSuccess() {
                        dialog.dismiss()
                        takePhoto(getAutoChangedPhotoUri())
                    }

                    override fun onUserCancel(scene: String, permissionGroupID: String, permission: String) {
                        dialog.dismiss()
                    }

                    override fun onUserDeny(scene: String, permissionGroupID: String, permission: String) {
                        dialog.dismiss()
                    }

                },
                AAFPermissionManager.takePhotoPermission
        )
    }

    view.choosePhotoBtn.setOnClickListener {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.Q) {
            choosePhoto()
        } else {
            PermissionManager.checkPermission(this, "PhotoSelect", false, object : PermissionManager.OnPermissionResult {
                override fun onFailed(msg: String) {
                    dialog.dismiss()
                }

                override fun onSuccess() {
                    dialog.dismiss()
                    choosePhoto()
                }

                override fun onUserCancel(scene: String, permissionGroupID: String, permission: String) {
                    dialog.dismiss()
                }

                override fun onUserDeny(scene: String, permissionGroupID: String, permission: String) {
                    dialog.dismiss()
                }
            }, AAFPermissionManager.selectPhotoPermission)
        }
    }
}

