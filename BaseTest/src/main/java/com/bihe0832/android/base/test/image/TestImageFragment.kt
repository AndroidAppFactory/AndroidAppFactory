package com.bihe0832.android.base.test.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.base.test.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.ui.image.BitmapUtil
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

        test_image_local_source.setImageBitmap(
            BitmapUtil.getLocalBitmap(
                context,
                R.mipmap.icon_author,
                1
            )
        )


        DownloadFile.startDownload(
            context!!,
            "http://up.deskcity.org/pic_source/18/2e/04/182e04f62f1aebf9089ed2275d26de21.jpg", true,
            object : SimpleDownloadListener() {
                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    ZLog.e(item.toString())

                }

                override fun onComplete(filePath: String, item: DownloadItem) {
                    BitmapUtil.getLocalBitmap(filePath).let {
                        test_image_remote_source.post {
                            test_image_remote_source.setImageBitmap(it)
                            BitmapUtil.compress(it, 20).let { aa ->
                                test_image_local_target.setImageBitmap(aa)
                                var fileSource = BitmapUtil.saveBitmapToSdCard(context!!, it)
                                var fileTarget = BitmapUtil.saveBitmapToSdCard(context!!, aa)
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

        test_basic_button.setOnClickListener {
            test_image_local_target.setImageBitmap(BitmapUtil.getRemoteBitmap("http://up.deskcity.org/pic_source/18/2e/04/182e04f62f1aebf9089ed2275d26de21.jpg", 720,720))
        }
    }

    override fun onResume() {
        super.onResume()
        initView()
    }

}