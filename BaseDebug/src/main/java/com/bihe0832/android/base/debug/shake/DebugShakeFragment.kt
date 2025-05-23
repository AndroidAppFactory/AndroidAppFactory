package com.bihe0832.android.base.debug.shake

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.View
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseFragment
import kotlin.math.sqrt

class DebugShakeFragment : BaseFragment() {

    // 传感器管理器
    private var mSensorManager: SensorManager? = null

    // 手机上一个位置时重力感应坐标
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    // 上次检测时间
    private var lastUpdateTime: Long = 0
    private var max = 0


    override fun getLayoutID(): Int {
        return R.layout.fragment_shake
    }

    override fun initView(view: View) {
        super.initView(view)
        view.findViewById<View>(R.id.shake_start).setOnClickListener {
            start(view.context)
        }

        view.findViewById<View>(R.id.shake_end).setOnClickListener {
            stop()
        }
    }

    fun start(context: Context) {
        // 获得传感器管理器
        max = 0
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        // 获得重力传感器
        mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            mSensorManager?.registerListener(
                mSensorEventListener, it, SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    // 停止检测
    fun stop() {
        mSensorManager?.unregisterListener(mSensorEventListener)
        mSensorManager = null
        view?.findViewById<TextView>(R.id.shake_result)?.setText("最大加速度：${max / 100}，${max}L / 100")
    }

    fun showResult(speed: Double) {
        if (speed > max) {
            max = speed.toInt()
        }
        view?.findViewById<TextView>(R.id.shake_result)?.setText("实时数值：" + speed.toInt() / 100)
    }

    private val mSensorEventListener = object : SensorEventListener {
        // 重力感应器感应获得变化数据
        override fun onSensorChanged(event: SensorEvent) {
            // 现在检测时间
            val currentUpdateTime = System.currentTimeMillis()
            // 两次检测的时间间隔
            val timeInterval = currentUpdateTime - lastUpdateTime

            // 判断是否达到了检测时间间隔
            if (timeInterval < 100) {
                return
            }
            // 现在的时间变成last时间
            lastUpdateTime = currentUpdateTime

            // 获得x,y,z坐标
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // 获得x,y,z的变化值
            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ

            // 将现在的坐标变成last坐标
            lastX = x
            lastY = y
            lastZ = z
            val speed =
                sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / timeInterval * 10000
            showResult(speed)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}


    }
}
