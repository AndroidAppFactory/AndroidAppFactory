package com.bihe0832.android.lib.device.shake

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

open class ShakeManagerImpl {

    // 速度阈值，当摇晃速度达到这值后产生作用
    val SPEED_SHRESHOLD = 5000L

    // 摇晃中两次检测位置的时间间隔
    val UPTATE_INTERVAL_TIME = 100

    // 传感器管理器
    private var mSensorManager: SensorManager? = null

    // 重力感应监听器
    private var mOnShakeListener: OnShakeListener? = null

    // 手机上一个位置时重力感应坐标
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    // 上次检测时间
    private var lastUpdateTime: Long = 0
    private var speedPerInterval: Long = SPEED_SHRESHOLD
    private var timeInterval = UPTATE_INTERVAL_TIME


    private val mSensorEventListener = object : SensorEventListener {
        // 重力感应器感应获得变化数据
        override fun onSensorChanged(event: SensorEvent) {
            // 现在检测时间
            val currentUpdateTime = System.currentTimeMillis()
            // 两次检测的时间间隔
            val interval = currentUpdateTime - lastUpdateTime

            // 判断是否达到了检测时间间隔
            if (interval < timeInterval) {
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
                sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / interval * 10000
            onShake(speed, timeInterval)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    }

    fun setSpeedAndInterval(maxSpeed: Long, interval: Int) {
        speedPerInterval = maxSpeed
        timeInterval = interval
    }

    open fun start(context: Context) {
        // 获得传感器管理器
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        // 获得重力传感器
        mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            mSensorManager?.registerListener(
                mSensorEventListener, it, SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    // 停止检测
    open fun stop() {
        mSensorManager?.unregisterListener(mSensorEventListener)
        mSensorManager = null
    }

    open fun onShake(speed: Double, time: Int) {
        if (speed >= speedPerInterval) {
            mOnShakeListener?.onShake()
        }
    }

    // 摇晃监听接口
    interface OnShakeListener {
        fun onShake()
    }

    // 设置重力感应监听器
    fun setOnShakeListener(listener: OnShakeListener?) {
        mOnShakeListener = listener
    }
}