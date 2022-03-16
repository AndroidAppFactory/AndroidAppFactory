package com.bihe0832.android.base.test.image

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.base.test.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.ui.image.BitmapUtil
import com.bihe0832.android.lib.ui.image.HeadIconBuilder
import com.bihe0832.android.lib.ui.image.loadCircleCropImage
import com.bihe0832.android.lib.ui.image.loadRoundCropImage
import kotlinx.android.synthetic.main.fragment_test_image.*
import java.io.File

class TestImageFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test_image, container, false)
    }

    fun initView() {

//        test_image_local_source.setImageBitmap(
//            BitmapUtil.getLocalBitmap(
//                context,
//                R.mipmap.icon_author,
//                1
//            )
//        )

        test_image_local_source.loadRoundCropImage(R.mipmap.a,120)

        var path = ""
        DownloadFile.startDownload(
            context!!,
            "http://up.deskcity.org/pic_source/18/2e/04/182e04f62f1aebf9089ed2275d26de21.jpg", true,
            object : SimpleDownloadListener() {
                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    ZLog.e(item.toString())

                }

                override fun onComplete(filePath: String, item: DownloadItem) {
                    path = filePath
                    BitmapUtil.getLocalBitmap(filePath).let {
                        test_image_remote_source.post {
                            test_image_remote_source.setImageBitmap(it)
                            BitmapUtil.compress(it, 20).let { aa ->
                                test_image_local_target.setImageBitmap(aa)
                                var fileSource = BitmapUtil.saveBitmap(context!!, it)
                                var fileTarget = BitmapUtil.saveBitmap(context!!, aa)
                                ZLog.e(
                                    "BitmapUtil",
                                    "onComplete filePath:$filePath : " + File(filePath).length()
                                )
                                ZLog.e(
                                    "BitmapUtil",
                                    "onComplete fileSource:$fileSource :" + File(fileSource).length()
                                )
                                ZLog.e(
                                    "BitmapUtil",
                                    "onComplete fileTarget:$fileTarget :" + File(fileTarget).length()
                                )

                            }

                        }

                    }
                }

                override fun onProgress(item: DownloadItem) {

                }

            })

        var num = 0
        test_basic_button.setOnClickListener {
            var headIconBuilder = HeadIconBuilder(context!!).apply {
                setImageUrls(mutableListOf<Any>().apply {
                    for (i in 0..num) {
                        if (TextUtils.isEmpty(path)) {
                            add("http://cdn.bihe0832.com/images/head.jpg")
                        } else {
                            add(
                                ZixieFileProvider.getZixieFileProvider(
                                    context!!,
                                    File(path)
                                )
                            )
                        }
                    }
                } as List<String>)
                setItemWidth(720)
            }
            headIconBuilder.generateBitmap { bitmap, filePath ->
                test_image_local_target.loadCircleCropImage(filePath)
                test_image_local_source.setImageBitmap(bitmap)
            }
            num++
//            test_image_local_target.setImageBitmap(BitmapUtil.getRemoteBitmap("http://up.deskcity.org/pic_source/18/2e/04/182e04f62f1aebf9089ed2275d26de21.jpg", 720,720))
        }
    }

    override fun onResume() {
        super.onResume()
        initView()
    }

}