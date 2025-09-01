package com.bihe0832.android.base.debug.view

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground
import com.bihe0832.android.lib.ui.custom.view.background.changeStatusWithUnreadMsg
import com.bihe0832.android.lib.ui.textview.ext.setDrawableLeft
import com.bihe0832.android.lib.ui.textview.marquee.MarqueeTextView
import com.bihe0832.android.lib.ui.textview.marquee.MarqueeTextView.OnScrollListener
import com.bihe0832.android.lib.ui.textview.span.ZixieTextClickableSpan
import com.bihe0832.android.lib.ui.textview.span.ZixieTextImageSpan
import com.bihe0832.android.lib.ui.textview.span.ZixieTextRadiusBackgroundSpan
import com.bihe0832.android.lib.utils.os.DisplayUtil

class DebugTextViewFragment : BaseFragment() {
    private var index = 0

    override fun getLayoutID(): Int {
        return R.layout.fragment_test_text
    }

    var testList = mutableListOf<String>(
        "这是一个测个测个测测个测测个fdsfsdsf测个测个测试测试0 fdsfsdf\ndsd ",
        "这是一个测试测试这是一个测试测试这是一个测试1",
        "这是一个测试测试这是一个测试测试这是一个测试测试这是一",
        "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试3",
        "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试测试4",
        "这是一个两个个三个四个五测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是测试5",
        "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这试测试6",
        "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试试这是这是一个测试测7",
        "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个试测这是一个试测这是一个试测这是一个试测试这是一个测试测试8",
    )

    override fun initView(view: View) {
        view.findViewById<MarqueeTextView>(R.id.text_marquee).apply {
//            background = getDrawable(intArrayOf(Color.RED, Color.YELLOW), GradientDrawable.Orientation.LEFT_RIGHT, DisplayUtil.dip2px(context, 4f).toFloat(), DisplayUtil.dip2px(context, 2f), Color.BLUE)
            setDrawableLeft(R.drawable.icon_menu, DisplayUtil.dip2px(context, 16f), DisplayUtil.dip2px(context, 16f))
            text = ":fds"
            startScroll()
            setOnScrollListener(object : OnScrollListener {
                override fun OnComplete() {
                    text = text.toString() + "fdsffsd "
                }

                override fun onPause() {
                    ZLog.d("onPause")
                }

                override fun onStop() {
                    ZLog.d("onPause")
                }

                override fun onStart() {
                    ZLog.d("onPause")
                }
            })
        }

        var text = "这是普通颜色文字" + TextFactoryUtils.getSpecialText("这是高亮测试", Color.parseColor("#E66633"))

        view.findViewById<TextView>(R.id.info_content_drawable).apply {

            setText(
                TextFactoryUtils.getCharSequenceWithClickAction(
                    text,
                    "这是高亮测试",
                    object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            ZixieContext.showToast(text)
                        }
                    },
                ),
            )
            movementMethod = LinkMovementMethod.getInstance()
            //            setText(TextFactoryUtils.getSpannedTextByHtml(TextFactoryUtils.getSpecialText("这是一个测试",Color.WHITE)))
//            setDrawable(
//                    R.drawable.icon,
//                    R.drawable.icon,
//                    R.drawable.icon,
//                    R.drawable.icon,
//                    DisplayUtil.dip2px(context!!, 30f),
//                    DisplayUtil.dip2px(context!!, 30f)
        }


        testSpecialText(testList[0])
//        info_content_0.setText(TextFactoryUtils.getTextHtmlAfterTransform("这是一个         一个测试                 fdsfsdf\ndsd   fdf "))

//        rtv_msg_tip.setDrawableBackground(resources.getColor(R.color.red_dot), 15, 0, Color.parseColor("#0000ff"))
        view.findViewById<TextViewWithBackground>(R.id.rtv_msg_tip)
                .changeStatusWithUnreadMsg(90, DisplayUtil.dip2px(context, 8f))
        view.findViewById<Button>(R.id.test_basic_button).apply {
            var num = -1
            setOnClickListener {
                view.findViewById<TextViewWithBackground>(R.id.rtv_msg_tip)
                        .changeStatusWithUnreadMsg(num, DisplayUtil.dip2px(context, 8f))

//                setTextColor(Color.WHITE)
                num++
//                when (num) {
//                    1 -> {
//                        setCornerRadiusBackground(5)
//                    }
//
//                    2 -> {
//                        setStrokeBackground(3, Color.RED)
//                    }
//
//                    3 -> {
//                        setCornerRadiusBackground(Color.GREEN, 15)
//                    }
//
//                    4 -> {
//                        setDrawableBackground(Color.YELLOW, 15, 3, Color.GREEN)
//                    }
//
//                    5 -> {
//                        setStrokeBackground(15, 3, Color.GREEN)
//                    }
//
//                }
//
//                testSpecialText(testList[5])
//                info_content_1.text = testList[index + 0]
//                info_content_1.setExpandText(":fsdfsdfsd")
//                info_content_2.text = testList[index + 1]
//                info_content_3.text = testList[index + 2]
//                index += 3
//                if (index > 7) {
//                    index = 0
//                }
            }
        }
    }

    fun testFun() {
        SpannableStringBuilder("a").apply {
            append("aaaaaaa")
            setSpan(
                ZixieTextImageSpan(
                    context!!,
                    BitmapUtil.getLocalBitmap(
                        context!!,
                        R.drawable.icon_author,
                        1,
                    ),
                ),
                2,
                3,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE,
            )
            append("aaaaaaa")
        }.let {
            view!!.findViewById<TextView>(R.id.info_content_0).text = it
        }

        SpannableStringBuilder("测试").apply {
            append("测试测试测试测试测试")
            setSpan(
                ZixieTextImageSpan(
                    context!!,
                    BitmapUtil.getLocalBitmap(context!!, R.drawable.icon_author, 1),
                ),
                2,
                3,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE,
            )
            append("这是一个测试")
        }.let {
            view!!.findViewById<TextView>(R.id.info_content_00).text = it
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
                Color.YELLOW,
                Color.RED,
                2,
                10,
                60,
                20,
                20,
                view!!.findViewById<TextView>(R.id.info_content_00).textSize * 3 / 5,
                0,
                Typeface.DEFAULT,
            ),
//                ZixieTextRadiusBackgroundSpan(
//                        Color.parseColor("#998D8D8D"),
//                        Color.parseColor("#998D8D8D"),
//                        0,
//                        DisplayUtil.dip2px(ZixieContext.applicationContext, 4f),
//                        DisplayUtil.dip2px(ZixieContext.applicationContext, 20f),
//                        8,
//                        DisplayUtil.dip2px(ZixieContext.applicationContext, 20f),
//                        DisplayUtil.dip2px(ZixieContext.applicationContext, 18f).toFloat(),
//                        Color.WHITE,
//                        Typeface.DEFAULT_BOLD
//                )
            start,
            end,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE,
        )

        spanString.setSpan(
            ZixieTextClickableSpan(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    ZixieContext.showToast("test")
                }
            }),
            start,
            end,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE,
        )

//        }

        view!!.findViewById<TextView>(R.id.info_content_0).text = spanString

//        SpannableStringBuilder(" ").apply {
//
//            append("1")
// //            setSpan(
// //                ZixieTextImageSpan(
// //                    context!!,
// //                    R.drawable.ic_left_arrow_white
// //                ),
// //                0,
// //                1,
// //                Spannable.SPAN_INCLUSIVE_INCLUSIVE
// //            )
//            setSpan(
//                    ZixieTextImageSpan(
//                            context!!,
//                            BitmapUtil.getLocalBitmap(context!!,
//                                    R.drawable.icon_author, 1)
//                    ),
//                    0,
//                    1,
//                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
//            )
//            append("Fsdfsdfd")
//            setSpan(
//                    ZixieTextClickableSpan(object : View.OnClickListener {
//                        override fun onClick(v: View?) {
//                            ZixieContext.showToast("文字")
//                        }
//
//                    }), 3,
//                    4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
//            )
//        }.let {
//            info_content_0.append(it)
// //            info_content_0.setMovementMethod(LinkMovementMethod.getInstance());
//
//        }

//        info_content_0.append(
//                TextFactoryUtils.getSpannedTextByHtml(
//                        TextFactoryUtils.getTextHtmlAfterTransform(
//                                "这是一个         一个测试                 fdsfsdf\ndsd   fdf "
//                        )
//                )
//        )
    }
}
