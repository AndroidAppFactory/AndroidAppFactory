package com.bihe0832.android.base.debug.temp

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.lib.ace.editor.AceSelectionActionHelper
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.ui.image.loadImage
import com.bihe0832.android.lib.ui.image.loadRoundCropImage
import com.bihe0832.android.lib.ui.menu.PopMenu
import com.bihe0832.android.lib.ui.menu.PopMenuItem
import com.bihe0832.android.lib.ui.menu.PopupList
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_test_basic.*

class DebugBasicFragment : BaseFragment() {

    override fun getLayoutID(): Int {
        return R.layout.fragment_test_basic
    }

    override fun initView(view: View) {
        super.initView(view)


        test_basic_button.setOnClickListener {
            showPopList()

//            test_basic_content.loadFitCenterImage("http://cdn.bihe0832.com/images/cv.png")
        }

        test_basic_button_local_1.setOnClickListener {
            AceSelectionActionHelper(activity!!).apply {
                setOnSelectionItemPressedListener(object : AceSelectionActionHelper.OnSelectionItemPressed{
                    override fun onCutClick() {
                        ZixieContext.showToast("onCutClick")
                    }

                    override fun onCopyClick() {
                        ZixieContext.showToast("onCopyClick")
                    }

                    override fun onPasteClick() {
                        ZixieContext.showToast("onPasteClick")
                    }

                    override fun onSelectAllClick() {
                        ZixieContext.showToast("onSelectAllClick")
                    }

                    override fun onSearchClick() {
                        ZixieContext.showToast("onSearchClick")
                    }

                })
            }.show(it,it.getX() + it.getWidth() / 2, 0f)
//            test_basic_content.loadCenterCropImage("http://cdn.bihe0832.com/images/cv.png")
        }

        test_basic_button_local_2.setOnClickListener {
            test_basic_content.loadImage("http://cdn.bihe0832.com/images/zixie_32.ico", true, Color.GRAY, Color.GRAY, RequestOptions().optionalCircleCrop())
        }

        test_basic_button_local_3.setOnClickListener {
            test_basic_content.loadRoundCropImage("http://cdn.bihe0832.com/images/zixie_32.ico", DisplayUtil.dip2px(context, 3f))
        }
    }

    private fun showPopList() {
        mutableListOf<String>().apply {
            add("复制")
            add("粘贴")
            add("删除")
            add("删除")
            add("删除")
            add("删除")
            add("删除")
            add("删除")
            add("删除")

        }.let {
            PopupList(activity!!).apply {
                textSize = DisplayUtil.dip2px(context!!, 12f).toFloat()
            }.show(test_basic_button, 600f, 0f, true,it, object : PopupList.PopupListListener {


                override fun onPopupListClick(
                        contextView: View?,
                        contextPosition: Int,
                        position: String
                ) {
                    ZixieContext.showToast(position)
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
                RouterAction.openFinalURL(router)
            }
        })
        return action
    }

    companion object {
        private const val TAG = "TestCardActivity-> "
    }
}