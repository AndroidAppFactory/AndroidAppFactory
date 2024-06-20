package com.bihe0832.android.common.main

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bihe0832.android.common.navigation.drawer.NavigationDrawerFragment
import com.bihe0832.android.common.navigation.drawer.R
import com.bihe0832.android.common.qrcode.QrcodeUtils
import com.bihe0832.android.framework.ui.main.CommonRootActivity
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground
import com.bihe0832.android.lib.ui.custom.view.background.changeStatusWithUnreadMsg
import com.bihe0832.android.lib.utils.os.DisplayUtil

open class CommonActivityWithNavigationDrawer : CommonRootActivity() {

    open fun getNavigationDrawerFragment(): NavigationDrawerFragment? {
        return CommonNavigationDrawerFragment()
    }

    override fun getLayoutID(): Int {
        return R.layout.com_bihe0832_activity_navigation_drawer
    }

    private val mNavigationDrawerFragment by lazy {
        getNavigationDrawerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNavigationDrawerFragment?.apply {
            setUp(findViewById(R.id.navigation_drawer_fl), findViewById(R.id.drawer_layout))
        }

        findViewById<ImageView>(R.id.title_icon).apply {
            setOnClickListener {
                openDrawer()
            }
        }
        mNavigationDrawerFragment?.let {
            loadRootFragment(R.id.navigation_drawer_fl, it)
        }
    }

    fun showQrcodeScan(needSound: Boolean, needVibrate: Boolean, onlyQRCode: Boolean) {
        findViewById<ImageView>(R.id.title_scan).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                QrcodeUtils.openQrScanAndParse(needSound, needVibrate, onlyQRCode,true)
            }
        }
    }

    fun hideQrcodeScan() {
        findViewById<ImageView>(R.id.title_scan).apply {
            visibility = View.GONE
        }
    }

    fun openDrawer() {
        mNavigationDrawerFragment?.openDrawer()
    }

    fun closeDrawer() {
        mNavigationDrawerFragment?.closeDrawer()
    }

    override fun updateTitle(titleName: String?) {
        findViewById<TextView>(R.id.title_text).setText(titleName)
    }

    override fun updateIcon(iconURL: String?, iconRes: Int, listener: View.OnClickListener?) {
        findViewById<ImageView>(R.id.title_icon)?.apply {
            updateIcon(this, iconURL, iconRes)
            setOnClickListener(listener)
        }
    }

    fun setUnReadNum(num: Int) {
        findViewById<TextViewWithBackground>(R.id.title_icon_unread).changeStatusWithUnreadMsg(
            num,
            DisplayUtil.dip2px(this, 8f),
        )
    }

    fun updateNum(num: Int) {
        if (num > 0) {
            setUnReadNum(num)
        } else {
            setUnReadNum(-1)
        }
    }
}
