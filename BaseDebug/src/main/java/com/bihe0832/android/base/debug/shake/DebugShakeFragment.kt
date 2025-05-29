package com.bihe0832.android.base.debug.shake

import android.content.Context
import android.view.View
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.device.shake.ShakeManagerImpl

class DebugShakeFragment : BaseFragment() {

    private var max = 0

   private val  mShakeManagerImpl  = object : ShakeManagerImpl() {
       override fun onShake(speed: Double, time: Int) {
           super.onShake(speed, time)
           showResult(speed)
       }

       override fun stop() {
           super.stop()
           view?.findViewById<TextView>(R.id.shake_result)?.setText("最大加速度：${max / 100}，${max}L / 100")
       }

       override fun start(context: Context) {
           super.start(context)
           max = 0
       }
   }


    override fun getLayoutID(): Int {
        return R.layout.fragment_shake
    }

    override fun initView(view: View) {
        super.initView(view)
        view.findViewById<View>(R.id.shake_start).setOnClickListener {
            mShakeManagerImpl.start(view.context)
        }

        view.findViewById<View>(R.id.shake_end).setOnClickListener {
            mShakeManagerImpl.stop()
        }
    }



    fun showResult(speed: Double) {
        if (speed > max) {
            max = speed.toInt()
        }
        view?.findViewById<TextView>(R.id.shake_result)?.setText("实时数值：" + speed.toInt() / 100)

    }
}
