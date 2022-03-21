package com.bihe0832.android.base.test.textview

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.base.test.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction.openFinalURL
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.text.span.ZixieTextClickableSpan
import com.bihe0832.android.lib.text.span.ZixieTextImageSpan
import com.bihe0832.android.lib.text.span.ZixieTextRadiusBackgroundSpan
import com.bihe0832.android.lib.ui.menu.PopMenu
import com.bihe0832.android.lib.ui.menu.PopMenuItem
import com.bihe0832.android.lib.ui.menu.PopupList
import com.bihe0832.android.lib.utils.os.DisplayUtil
import kotlinx.android.synthetic.main.fragment_test_text.*


class TestTextView : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test_text, container, false)
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
    var index = 6
    fun initView() {

//        info_content_0.setText(TextFactoryUtils.getSpannedTextByHtmlAfterTransform("这是一个         一个测试                 fdsfsdf\ndsd   fdf "))
        testSpecialText(testList[5])

//        test_basic_button.setOnClickListener {
////            showMenu()
////            info_content_1.text = testList[index + 0]
////            info_content_1.setExpandText(":fsdfsdfsd")
////            info_content_2.text = testList[index + 1]
////            info_content_3.text = testList[index + 2]
////            index += 3
////            if (index > 7) {
////                index = 0
////            }
//        }
    }

    fun testSpecialText(content: String) {
        val spanString = SpannableString(content)
        var startIndex = 0
//        while (content.indexOf("测试", startIndex, true) > 0) {
        var start: Int = content.indexOf("测试", startIndex, true)
        var end = start + "测试".length
        spanString.setSpan(
            ZixieTextRadiusBackgroundSpan(
                Color.GREEN,
                20,
                Color.RED,
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


        info_content_0.setText(spanString)

        info_content_0.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableStringBuilder("文字holder文字").apply {
            setSpan(
                ZixieTextImageSpan(
                    context!!,
                    R.mipmap.ic_left_arrow_white
                ),
                0,
                1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }.let {
            info_content_0.append(it)
        }

        info_content_0.append(
            TextFactoryUtils.getSpannedTextByHtml(
                TextFactoryUtils.getTextHtmlAfterTransform(
                    "这是一个         一个测试                 fdsfsdf\ndsd   fdf "
                )
            )
        )
    }

    override fun onResume() {
        super.onResume()
        initView()
    }


    private fun showPopList() {
        mutableListOf<String>().apply {
            add("复制")
            add("粘贴")
            add("删除")
        }.let {
            PopupList(activity!!).apply {
                textSize = DisplayUtil.dip2px(context!!, 12f).toFloat()
            }.show(test_basic_button, it, object : PopupList.PopupListListener {
                override fun showPopupList(
                    adapterView: View?,
                    contextView: View?,
                    contextPosition: Int
                ): Boolean {
                    return true
                }

                override fun onPopupListClick(
                    contextView: View?,
                    contextPosition: Int,
                    position: Int
                ) {
                    ZixieContext.showToast(it.get(position))
                }
            })
        }
    }

    private fun showMenu() {
        PopMenu(activity!!, test_basic_button).apply {
            val menuActions: MutableList<PopMenuItem?> = ArrayList()

            menuActions.add(getNewPopMenuItem("群聊", R.mipmap.icon, ""))
            menuActions.add(getNewPopMenuItem("加好友", R.mipmap.icon, ""))
            menuActions.add(getNewPopMenuItem("创建群", R.mipmap.icon, ""))

            setMenuItemList(menuActions)
        }.let {
            it.show()
        }
    }

    private fun getNewPopMenuItem(stringRes: String, iconRes: Int, router: String): PopMenuItem? {
        val action = PopMenuItem()
        action.setActionName(stringRes)
        action.setIconResId(iconRes)
        action.setItemClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(router)) {
                ZixieContext.showWaiting()
            } else {
                openFinalURL(router)
            }
        })
        return action
    }


    companion object {
        private const val TAG = "TestCardActivity-> "
    }
}