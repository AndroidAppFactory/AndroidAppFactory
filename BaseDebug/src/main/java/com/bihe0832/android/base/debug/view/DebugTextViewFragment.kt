package com.bihe0832.android.base.debug.view

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.textview.span.ZixieTextClickableSpan
import com.bihe0832.android.lib.ui.textview.span.ZixieTextImageSpan
import com.bihe0832.android.lib.ui.textview.span.ZixieTextRadiusBackgroundSpan
import kotlinx.android.synthetic.main.fragment_test_text.*


class DebugTextViewFragment : BaseFragment() {

    override fun getLayoutID(): Int {
        return R.layout.fragment_test_text
    }

    var testList = mutableListOf<String>(
            "这是一个测试测试0",
            "这是一个测试测试这是一个测试测试这是一个测试1",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试3",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试测试4",
            "这是一个两个个三个四个五测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是测试5",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这试测试6",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试试这是这是一个测试测7",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个试测这是一个试测这是一个试测这是一个试测试这是一个测试测试8"
    )

    override fun initView(view: View) {
        info_content_0.setText(TextFactoryUtils.getTextHtmlAfterTransform("这是一个         一个测试                 fdsfsdf\ndsd   fdf "))
        testSpecialText(testList[5])

        test_basic_button.setOnClickListener {
//            info_content_1.text = testList[index + 0]
//            info_content_1.setExpandText(":fsdfsdfsd")
//            info_content_2.text = testList[index + 1]
//            info_content_3.text = testList[index + 2]
//            index += 3
//            if (index > 7) {
//                index = 0
//            }
        }

        checkbox1.setOnClickListener {
            checkbox1.toggle()
        }
    }


    fun testSpecialText(content: String) {
        val spanString = SpannableString(content)
        var startIndex = 0
//        while (content.indexOf("测试", startIndex, true) > 0) {
        var start: Int = content.indexOf("测试", startIndex, true)
        var end = start + "测试".length
        spanString.setSpan(
                ZixieTextRadiusBackgroundSpan(
                        Color.RED,
                        10,
                        8,
                        12,
                        8,
                        info_content_0.textSize * 3 / 5,
                        0,
                        Typeface.DEFAULT_BOLD
                ),
                start,
                end,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        spanString.setSpan(
                ZixieTextClickableSpan(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        ZixieContext.showToast("test")
                    }

                }), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );

//        }


        info_content_0.setText("")

        SpannableStringBuilder(" ").apply {

            append("1")
//            setSpan(
//                ZixieTextImageSpan(
//                    context!!,
//                    R.mipmap.ic_left_arrow_white
//                ),
//                0,
//                1,
//                Spannable.SPAN_INCLUSIVE_INCLUSIVE
//            )
            setSpan(
                    ZixieTextImageSpan(
                            context!!,
                            com.bihe0832.android.lib.ui.image.BitmapUtil.getLocalBitmap(context!!,
                                    R.mipmap.icon_author, 1)
                    ),
                    0,
                    1,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            append("Fsdfsdfd")
            setSpan(
                    ZixieTextClickableSpan(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            ZixieContext.showToast("文字")
                        }

                    }), 3,
                    4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }.let {
            info_content_0.append(it)
//            info_content_0.setMovementMethod(LinkMovementMethod.getInstance());

        }

        info_content_0.append(
                TextFactoryUtils.getSpannedTextByHtml(
                        TextFactoryUtils.getTextHtmlAfterTransform(
                                "这是一个         一个测试                 fdsfsdf\ndsd   fdf "
                        )
                )
        )
    }




    companion object {
        private const val TAG = "TestCardActivity-> "
    }
}