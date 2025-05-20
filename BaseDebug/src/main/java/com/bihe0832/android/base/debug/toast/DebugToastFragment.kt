package com.bihe0832.android.base.debug.toast

import android.graphics.Color
import android.view.View
import android.widget.Toast
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.intent.IntentUtils

class DebugToastFragment : DebugEnvFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("Toast测试", View.OnClickListener {
                ToastUtil.showTop(
                    context,
                    "这是一个测试用的<font color ='#38ADFF'><b>测试消息</b></font>",
                    Toast.LENGTH_LONG
                )
            }))
            add(
                getDebugItem(
                    "普通Toast",
                    View.OnClickListener { ToastUtil.showShort(context!!, "这是一个普通Toast") })
            )
            add(
                getDebugItem(
                    "顶部Toast",
                    View.OnClickListener {
                        ToastUtil.showTop(
                            context!!,
                            "这是一个顶部Toast",
                            Toast.LENGTH_SHORT
                        )
                    })
            )
            add(getDebugItem("Tips Toast", View.OnClickListener {
                ToastUtil.showTips(context!!, resources.getDrawable(R.mipmap.icon).apply {
                    setTint(Color.RED)
                }, "执行成功", Toast.LENGTH_SHORT)
            }))
            add(
                getDebugItem(
                    "调试版本",
                    View.OnClickListener { ZixieContext.showDebugEditionToast() })
            )
            add(getDebugItem("敬请期待", View.OnClickListener { ZixieContext.showWaiting() }))
            add(
                getDebugItem(
                    "打开Activity Toast 立即关闭",
                    View.OnClickListener { startActivityWithException(DebugToastActivity::class.java) })
            )
            add(getDebugItem("仅前台Toast", View.OnClickListener {
                ZixieContext.showLongToastJustAPPFront("这是一个仅前台Toast")
                ThreadManager.getInstance().start({
                    IntentUtils.goHomePage(context!!)
                }, 3)
                ThreadManager.getInstance().start({
                    ZixieContext.showLongToastJustAPPFront("这是一个仅前台Toast")
                }, 5)
            }))
            add(getDebugItem("不分前后台Toast", View.OnClickListener {
                ZixieContext.showToast("这是一个不分前后台Toast")


                ThreadManager.getInstance().start({
                    ZixieContext.showToast("这是一个不分前后台Toast")
                }, 5)

                ThreadManager.getInstance().start({
                    IntentUtils.goHomePage(context!!)
                }, 3)

            }))

        }
    }
}