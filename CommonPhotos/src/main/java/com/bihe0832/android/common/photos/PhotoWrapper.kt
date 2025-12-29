package com.bihe0832.android.common.photos

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FILE_TYPE_IMAGE
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.OSUtils
import java.io.File
import com.bihe0832.android.model.res.R as ModelResR

fun getAutoChangedPhotoName(): String {
    return "zixie_" + System.currentTimeMillis() + ".jpg"
}

fun Context.getPhotosFolder(): String {
    return Media.getZixieMediaPath(this, FILE_TYPE_IMAGE, "")
}

fun Context.getAutoChangedPhotoUri(): Uri? {
    return getPhotosUri(getAutoChangedPhotoName())
}

fun Context.getAutoChangedCropUri(): Uri? {
    return getCropUri(getAutoChangedPhotoName())
}

fun Context.getCropUri(fileName: String): Uri? {
    return if (OSUtils.isAndroidQVersion()) {
        Media.createUriAboveAndroidQ(
            this,
            FILE_TYPE_IMAGE,
            "",
            fileName,
        )
    } else {
        Media.createUriBelowAndroidQ(
            this,
            "",
            fileName,
        )
    }
}

fun Context.getPhotosUri(fileName: String): Uri? {
    return if (OSUtils.isAndroidQVersion()) {
        Media.createUriAboveAndroidQ(
            this,
            FILE_TYPE_IMAGE,
            "",
            fileName,
        )
    } else {
        Media.createUriBelowAndroidQ(
            this,
            "",
            fileName,
        )
    }
}

/**
 * TargetFile 建议使用 [getPhotosFolder] 获取
 */
fun Context.getCropIntentPhoto(
    sourceFile: Uri?,
    targetFile: Uri?,
    aspectX: Int = 1,
    aspectY: Int = 1
): Intent {
    ZLog.d("Activity cropPhoto sourceFile ：$sourceFile")
    ZLog.d("Activity cropPhoto targetFile ：$targetFile")

    var finalSourceFile: Uri? = if (ZixieFileProvider.isZixieFileProvider(this, sourceFile)) {
        sourceFile
    } else {
        ZixieFileProvider.getZixieFileProvider(this, ZixieFileProvider.uriToFile(this, sourceFile))
    }

    val file = Media.uriToFile(this, targetFile, false)

    if (file != null) {
        FileUtils.checkAndCreateFolder(file.parent)
    }

    var outSizePerPart = 1080 / aspectX.coerceAtLeast(aspectY)
    // 开始切割
    return Intent("com.android.camera.action.CROP").apply {
        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setDataAndType(finalSourceFile, FILE_TYPE_IMAGE)
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

}

fun Activity.cropPhoto(sourceFile: String, targetFile: Uri?, aspectX: Int = 1, aspectY: Int = 1) {
    var sourceFileProvider = ZixieFileProvider.getZixieFileProvider(this, File(sourceFile))
    cropPhoto(sourceFileProvider, targetFile, aspectX, aspectY)
}

fun Fragment.cropPhoto(sourceFile: String, targetFile: Uri?, aspectX: Int = 1, aspectY: Int = 1) {
    var sourceFileProvider = ZixieFileProvider.getZixieFileProvider(context, File(sourceFile))
    cropPhoto(sourceFileProvider, targetFile, aspectX, aspectY)
}

fun Activity.cropPhoto(sourceFile: Intent?, targetFile: Uri?, aspectX: Int = 1, aspectY: Int = 1) {
    var file = ZixieFileProvider.uriToFile(this, sourceFile?.data)
    cropPhoto(ZixieFileProvider.getZixieFileProvider(this, file), targetFile, aspectX, aspectY)
}

fun Fragment.cropPhoto(sourceFile: Intent?, targetFile: Uri?, aspectX: Int = 1, aspectY: Int = 1) {
    var file = ZixieFileProvider.uriToFile(context, sourceFile?.data)
    cropPhoto(ZixieFileProvider.getZixieFileProvider(context, file), targetFile, aspectX, aspectY)
}

fun Activity.cropPhoto(sourceFile: Uri?, targetFile: Uri?, aspectX: Int = 1, aspectY: Int = 1) {
    val intent = getCropIntentPhoto(sourceFile, targetFile, aspectX, aspectY)
    startActivityForResult(intent, ZixieActivityRequestCode.CROP_PHOTO)
}

fun Fragment.cropPhoto(sourceFile: Uri?, targetFile: Uri?, aspectX: Int = 1, aspectY: Int = 1) {
    val intent = context?.getCropIntentPhoto(sourceFile, targetFile, aspectX, aspectY)
    startActivityForResult(intent, ZixieActivityRequestCode.CROP_PHOTO)
}

fun getTakePhotoIntent(outputUri: Uri?): Intent {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    return intent
}


fun Activity.takePhoto(outputUri: Uri?) {
    val intent = getTakePhotoIntent(outputUri)
    startActivityForResult(intent, ZixieActivityRequestCode.TAKE_PHOTO)
}

fun Fragment.takePhoto(outputUri: Uri?) {
    val intent = getTakePhotoIntent(outputUri)
    startActivityForResult(intent, ZixieActivityRequestCode.TAKE_PHOTO)
}

fun Activity.choosePhoto(fileType: String) {
    val intent = Intent(Intent.ACTION_PICK)
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileType)
    try {
        startActivityForResult(intent, ZixieActivityRequestCode.CHOOSE_PHOTO)
    } catch (e: Exception) {
        e.printStackTrace()
        ZixieContext.showDebug(getString(ModelResR.string.common_photo_choose_photo_bad))
    }
}

fun Fragment.choosePhoto(fileType: String) {
    val intent = Intent(Intent.ACTION_PICK)
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileType)
    try {
        startActivityForResult(intent, ZixieActivityRequestCode.CHOOSE_PHOTO)
    } catch (e: Exception) {
        e.printStackTrace()
        ZixieContext.showDebug(getString(ModelResR.string.common_photo_choose_photo_bad))
    }
}


fun Activity.choosePhoto() {
    choosePhoto(FILE_TYPE_IMAGE)
}

fun Fragment.choosePhoto() {
    choosePhoto(FILE_TYPE_IMAGE)
}


fun Activity.getPhotoContent() {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, FILE_TYPE_IMAGE)
    try {
        startActivityForResult(intent, ZixieActivityRequestCode.CHOOSE_PHOTO)
    } catch (e: Exception) {
        e.printStackTrace()
        ZixieContext.showDebug("未找到图片查看器")
    }
}

fun Fragment.getPhotoContent() {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, FILE_TYPE_IMAGE)
    try {
        startActivityForResult(intent, ZixieActivityRequestCode.CHOOSE_PHOTO)
    } catch (e: Exception) {
        e.printStackTrace()
        ZixieContext.showDebug("未找到图片查看器")
    }
}

fun Activity.showPhotoChooser() {
    showPhotoChooser({ takePhoto(getAutoChangedPhotoUri()) }, { choosePhoto() })
}

fun Fragment.showPhotoChooser() {
    context?.let {
        it.showPhotoChooser({ takePhoto(it.getAutoChangedPhotoUri()) }, { choosePhoto() })
    }
}

fun Context.showPhotoChooser(takePhotoAction: (() -> Unit), choosePhotoAction: (() -> Unit)) {
    val view = LayoutInflater.from(this).inflate(R.layout.com_bihe0832_dialog_photo_chooser, null)
    val dialog = AlertDialog.Builder(this).setView(view).create()
    dialog.setCanceledOnTouchOutside(true)
    dialog.show()

    view.findViewById<TextView>(R.id.takePhotoBtn).setOnClickListener {
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
                    takePhotoAction()
                }

                override fun onUserCancel(
                    scene: String,
                    permissionGroupID: String,
                    permission: String
                ) {
                    dialog.dismiss()
                }

                override fun onUserDeny(
                    scene: String,
                    permissionGroupID: String,
                    permission: String
                ) {
                    dialog.dismiss()
                }
            },
            AAFPermissionManager.takePhotoPermission,
        )
    }
    view.findViewById<TextView>(R.id.choosePhotoBtn).setOnClickListener {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.Q) {
            choosePhotoAction()
        } else {
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
                        choosePhotoAction()
                    }

                    override fun onUserCancel(
                        scene: String,
                        permissionGroupID: String,
                        permission: String
                    ) {
                        dialog.dismiss()
                    }

                    override fun onUserDeny(
                        scene: String,
                        permissionGroupID: String,
                        permission: String
                    ) {
                        dialog.dismiss()
                    }
                },
                AAFPermissionManager.selectPhotoPermission,
            )
        }
    }
}
