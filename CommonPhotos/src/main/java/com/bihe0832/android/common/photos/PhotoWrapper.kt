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
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.ZixieFileProvider
import com.bihe0832.android.lib.permission.PermissionManager
import kotlinx.android.synthetic.main.com_bihe0832_dialog_photo_chooser.view.*
import java.io.File


val takePhotoPermission = arrayOf(Manifest.permission.CAMERA)
val selectPhotoPermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)


fun Activity.getDefaultPhoto(): File {
    return File(getPhotosFolder(), "temp.jpg")
}

fun Activity.getPhotosFolder(): String {
    var filePath = ZixieContext.getZixieFolder() + File.separator + "pictures" + File.separator
    return if (FileUtils.checkAndCreateFolder(filePath)) {
        filePath
    } else {
        ""
    }
}

fun Activity.cropPhoto(sourceFile: String, targetFile: String, aspectX: Int = 1, aspectY: Int = 1) {
    var sourceFileProvider = ZixieFileProvider.getZixieFileProvider(this@cropPhoto, File(sourceFile))
    cropPhoto(sourceFileProvider, targetFile, aspectX, aspectY)
}

fun Activity.cropPhoto(sourceFile: Uri, targetFile: String, aspectX: Int = 1, aspectY: Int = 1) {
    File(targetFile).let {
        FileUtils.checkAndCreateFolder(it.parent)
        var outSizePerPart = 1080 / aspectX.coerceAtLeast(aspectY)
        // 开始切割
        val intent = Intent("com.android.camera.action.CROP").apply {
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(sourceFile, "image/*")
            putExtra("crop", "true")
            putExtra("aspectX", aspectX) // 裁剪框比例
            putExtra("aspectY", aspectY)
            putExtra("outputX", outSizePerPart * aspectX) // 输出图片大小
            putExtra("outputY", outSizePerPart * aspectY)
            putExtra("scale", true)
            putExtra("circleCrop", true)
            putExtra("return-data", false) // 不直接返回数据
            putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(it)) // 返回一个文件
            putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        }
        startActivityForResult(intent, ZixieActivityRequestCode.CROP_PHOTO)
    }
}

fun Activity.takePhoto(outputFile: File) {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    val outputUri = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            ZixieFileProvider.getZixieFileProvider(this, outputFile)
        }
        else -> Uri.fromFile(outputFile)
    }
    // 去拍照
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
    startActivityForResult(intent, ZixieActivityRequestCode.TAKE_PHOTO)
}

fun Activity.choosePhoto() {

    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    // 判断系统中是否有处理该 Intent 的 Activity
    if (intent.resolveActivity(packageManager) != null) {
        startActivityForResult(intent, ZixieActivityRequestCode.CHOOSE_PHOTO)
    } else {
        ZixieContext.showDebug("未找到图片查看器")
    }
}

fun Activity.showPhotoChooser() {
    val view = LayoutInflater.from(this).inflate(R.layout.com_bihe0832_dialog_photo_chooser, null)
    val dialog = AlertDialog.Builder(this).setView(view).create()
    dialog.setCanceledOnTouchOutside(true)
    dialog.show()

    view.takePhotoBtn.setOnClickListener {
        PermissionManager.checkPermission(this, "PhotoSelect", false, object : PermissionManager.OnPermissionResult {
            override fun onFailed(msg: String) {
                dialog.dismiss()
            }

            override fun onSuccess() {
                dialog.dismiss()
                takePhoto(getDefaultPhoto())
            }

            override fun onUserCancel(scene: String, permission: String) {
                dialog.dismiss()
            }

            override fun onUserDeny(scene: String, permission: String) {
                dialog.dismiss()
            }

        }, *takePhotoPermission)
    }

    view.choosePhotoBtn.setOnClickListener {
        PermissionManager.checkPermission(this, "PhotoSelect", false, object : PermissionManager.OnPermissionResult {
            override fun onFailed(msg: String) {
                dialog.dismiss()
            }

            override fun onSuccess() {
                dialog.dismiss()
                choosePhoto()
            }

            override fun onUserCancel(scene: String, permission: String) {
                dialog.dismiss()
            }

            override fun onUserDeny(scene: String, permission: String) {
                dialog.dismiss()
            }
        }, *selectPhotoPermission)
    }
}



