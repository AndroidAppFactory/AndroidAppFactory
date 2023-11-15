package com.bihe0832.android.base.debug.media

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.photos.choosePhoto
import com.bihe0832.android.common.photos.cropPhoto
import com.bihe0832.android.common.photos.getAutoChangedCropUri
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.media.image.BitmapUtil
import com.bihe0832.android.lib.media.image.loadImage
import kotlinx.android.synthetic.main.fragment_test_image.test_basic_button
import kotlinx.android.synthetic.main.fragment_test_image.test_image_local_source

public class DebugImageFragment : BaseFragment() {

    override fun getLayoutID(): Int {
        return R.layout.fragment_test_image
    }

    override fun initView(view: View) {
        val sourceFile = AAFFileWrapper.getTempFolder() + "cv_v.jpg"
        FileUtils.copyAssetsFileToPath(context, "cv_v.jpg", sourceFile)
        test_image_local_source.setImageBitmap(
            BitmapUtil.getLocalBitmap(
                sourceFile,
                300,
                600,
            ),
        )

//        test_image_local_source.loadRoundCropImage(R.mipmap.icon_author, 120)

//        test_image_local_source.loadImage(
//            "http://up.deskcity.org/pic_source/18/2e/04/182e04f62f1aebf9089ed2275d26de21.jpg",
//            100,
//            100,
//            R.drawable.icon_author,
//            R.drawable.icon_author,
//            false,
//            RequestOptions.bitmapTransform(
//                MultiTransformation(
//                    CenterInside(),
//                    BlurTransformation(
//                        context!!,
//                        150,
//                    ),
//                ),
//            ),
//        )
//
//        var path = ""
//        DownloadFile.download(
//            context!!,
//            "http://up.deskcity.org/pic_source/18/2e/04/182e04f62f1aebf9089ed2275d26de21.jpg",
//            true,
//            object : SimpleDownloadListener() {
//                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
//                    ZLog.e(item.toString())
//                }
//
//                override fun onComplete(filePath: String, item: DownloadItem): String {
//                    path = filePath
//                    BitmapUtil.getLocalBitmap(filePath).let {
//                        test_image_remote_source.post {
//                            test_image_remote_source.setImageBitmap(it)
//                            BitmapUtil.compress(it, 20).let { aa ->
//                                test_image_local_target.setImageBitmap(aa)
//                                var fileSource = BitmapUtil.saveBitmap(context!!, it)
//                                var fileTarget = BitmapUtil.saveBitmap(context!!, aa)
//                                ZLog.e(
//                                    "BitmapUtil",
//                                    "onComplete filePath:$filePath : " + File(filePath).length(),
//                                )
//                                ZLog.e(
//                                    "BitmapUtil",
//                                    "onComplete fileSource:$fileSource :" + File(fileSource).length(),
//                                )
//                                ZLog.e(
//                                    "BitmapUtil",
//                                    "onComplete fileTarget:$fileTarget :" + File(fileTarget).length(),
//                                )
//                            }
//                        }
//                    }
//                    return filePath
//                }
//
//                override fun onProgress(item: DownloadItem) {
//                }
//            },
//        )

        var num = 0
        test_basic_button.setOnClickListener {
            activity?.choosePhoto()
//            val filepath = ZixieContext.getLogFolder() + "aaa.jpg"
//            val headIconBuilder = HeadIconBuilder(context!!).apply {
//                setImageUrls(
//                    mutableListOf<Any>().apply {
//                        for (i in 0..num) {
//                            if (TextUtils.isEmpty(path)) {
//                                add("http://cdn.bihe0832.com/images/head.jpg")
//                            } else {
//                                add(
//                                    ZixieFileProvider.getZixieFileProvider(
//                                        context!!,
//                                        File(path),
//                                    ),
//                                )
//                            }
//                        }
//                    } as List<String>,
//                )
//                setItemWidth(720)
//            }
//            for (i in 0..50) {
//                ThreadManager.getInstance().start {
//                    HeadIconBuildFactory.generateBitmap(
//                        headIconBuilder,
//                        filepath,
//                        10 * 1000,
//                        call = HeadIconBuilder.GenerateBitmapCallback { p0, source ->
//                            ZLog.d(
//                                "zixieheadIconBuilder",
//                                "new:" + File(source).lastModified() + " " + DateUtil.getDateEN(File(source).lastModified()),
//                            )
// //                                test_image_local_source.setImageBitmap(BitmapUtil.getLocalBitmap(source))
//                            test_image_local_source.loadImage(
//                                source,
//                                Color.RED,
//                                Color.RED,
//                                720,
//                                720,
//                                true,
//                                DiskCacheStrategy.NONE,
//                                true,
//                                RequestOptions().circleCrop(),
//                            )
//                        },
//                    )
//                }
//            }
//            num++
        }
    }

    var cropUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK == resultCode) {
            ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode")
            when (requestCode) {
                ZixieActivityRequestCode.CHOOSE_PHOTO -> if (data != null && data.data != null) {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode $data")
                    cropUri = activity!!.getAutoChangedCropUri()
                    activity!!.cropPhoto(
                        data.getData(),
                        cropUri,
                        2,
                        1,
                    )
                } else {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：$requestCode；resultCode：$resultCode")
                }

                ZixieActivityRequestCode.CROP_PHOTO -> {
                    ZLog.d("PhotoChooser in PhotoChooser onResult requestCode：" + requestCode + "；resultCode：" + data.toString())

                    Media.uriToFile(activity!!, cropUri, true).absolutePath.let {
                        ZLog.d("PhotoChooser in cropUri：$it")
                        test_image_local_source.loadImage(it)
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
