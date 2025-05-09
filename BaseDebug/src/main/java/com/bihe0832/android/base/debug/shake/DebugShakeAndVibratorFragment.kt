package com.bihe0832.android.base.debug.shake

import android.app.Service
import android.os.Vibrator
import android.view.View
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.device.shake.ShakeManager

class DebugShakeAndVibratorFragment : DebugEnvFragment() {

    private var lastTime = 0L

    override fun initView(view: View) {
        super.initView(view)
        ShakeManager.setOnShakeListener(object : ShakeManager.OnShakeListener {
            override fun onShake() {
                if (System.currentTimeMillis() - lastTime > 1000L) {
                    lastTime = System.currentTimeMillis()
                    ZixieContext.showDebug("Shake 1")
                    (context?.getSystemService(Service.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(longArrayOf(50, 200, 100, 300), -1)
                }
            }
        })
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("摇一摇很灵敏", View.OnClickListener { ShakeManager.setSpeed(10)}))
            add(getDebugItem("摇一摇很迟钝", View.OnClickListener { ShakeManager.setSpeed(100)}))
            add(getDebugItem("摇一摇比较容易", View.OnClickListener { ShakeManager.setSpeed(30)}))
            add(getDebugItem("开启摇一摇", View.OnClickListener { ShakeManager.start(context!!) }))
            add(getDebugItem("关闭摇一摇", View.OnClickListener { ShakeManager.stop() }))
        }
    }
}