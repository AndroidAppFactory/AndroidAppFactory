package com.bihe0832.android.base.compose.debug.lock;

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.device.battery.BatteryHelper
import com.bihe0832.android.lib.device.battery.BatteryHelper.getBatteryStatus
import com.bihe0832.android.lib.lock.screen.service.LockScreenService
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.ui.custom.view.process.AccCircleProgress
import com.bihe0832.android.lib.ui.custom.view.slide.SlideFinishLayout
import com.bihe0832.android.lib.ui.dialog.view.ProgressIndicatorView
import com.bihe0832.android.lib.ui.textview.hint.HintTextView
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.os.BuildUtils.SDK_INT
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.lib.utils.time.DateUtil
import com.bihe0832.android.lib.widget.WidgetUpdateManager

class DebugLockActivity : BaseActivity() {

    private var lock_charge_indicator: ProgressIndicatorView? = null
    private var lock_charge_process: AccCircleProgress? = null
    private var lock_charge_text: TextView? = null

    private var lock_time: TextView? = null
    private var lock_date: TextView? = null

    private val mBatteryChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateChargeInfo()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        setContentView(R.layout.activity_lock)
        initView()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun getStatusBarColor(): Int {
        return resources.getColor(R.color.transparent)
    }

    override fun getNavigationBarColor(): Int {
        return resources.getColor(R.color.transparent)
    }

    private fun initView() {
        val layout = findViewById<SlideFinishLayout>(R.id.lock_root)
        layout.setOnSlidingFinishListener {
            finish()
        }
        lock_time = findViewById<TextView>(R.id.lock_time)
        lock_date = findViewById<TextView>(R.id.lock_date)
        lock_charge_text = findViewById<TextView>(R.id.lock_charge_text)
        lock_charge_indicator = findViewById<ProgressIndicatorView>(R.id.lock_charge_indicator).apply {
            setAnimationNum(3)
        }
        lock_charge_process = findViewById<AccCircleProgress>(R.id.lock_charge_process).apply {
            setArcBackgroundColor(intArrayOf(Color.GREEN, Color.GREEN))
            setCircleBackgroundColor(resources.getColor(R.color.md_theme_outline))
            setIconRes(R.drawable.icon_android)
            setCircleWidth(DisplayUtil.dip2px(context, 2f).toFloat())
            setArcWidth(DisplayUtil.dip2px(context, 2f).toFloat())
            setMax(100)
        }
        updateView()
        updateChargeInfo()
        initUpdateTask()
        initIconAnim()
        initLockAnim()
        BatteryHelper.startReceiveBatteryChanged(this, mBatteryChangedReceiver)
    }

    private fun updateChargeInfo() {
        ThreadManager.getInstance().runOnUIThread {
            getBatteryStatus(this).let { batteryStatus ->
                if (batteryStatus?.isCharging == true) {
                    var percent = (batteryStatus.getBatteryPercent() * 100).toInt()
                    lock_charge_indicator?.apply {
                        visibility = View.VISIBLE
                    }
                    lock_charge_process?.apply {
                        visibility = View.VISIBLE
                        serCurrentProcess(percent)
                    }

                    lock_charge_text?.apply {
                        text = percent.toString()
                        visibility = View.VISIBLE
                    }
                } else {
                    lock_charge_indicator?.visibility = View.INVISIBLE
                    lock_charge_process?.visibility = View.INVISIBLE
                    lock_charge_text?.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun initUpdateTask() {
        val currentTimeMillis = System.currentTimeMillis()
        val secondsPassed = (currentTimeMillis / 1000 % 60).toInt()
        val autoUpdate: BaseTask = object : BaseTask() {
            override fun getMyInterval(): Int {
                return 2 * 60
            }

            override fun getNextEarlyRunTime(): Int {
                return 2 * secondsPassed
            }

            override fun runAfterAdd(): Boolean {
                return false
            }

            override fun run() {
                updateView()
            }

            override fun getTaskName(): String {
                return TASK_NAME
            }
        }
        TaskManager.getInstance().addTask(autoUpdate)
        ZLog.d("TaskManager", autoUpdate.toString())
        TaskManager.getInstance().letTaskRunEarly(TASK_NAME)
        ZLog.d("TaskManager", autoUpdate.toString())
    }

    private fun updateView() {
        updateTime()
    }

    private fun updateTime() {
        ThreadManager.getInstance().runOnUIThread {
            val data = DateUtil.getCurrentWeekCN(System.currentTimeMillis(), "HH:mm-M月dd日 E")
            val date = data.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            lock_time?.text = date[0]
            lock_date?.text = date[1]
        }
    }

    private fun initLockAnim() {
        val textView = findViewById<HintTextView>(R.id.lock_action)
        textView.setLinearGradientColors(intArrayOf(Color.GRAY, Color.WHITE, Color.GRAY))
        textView.setLinearGradientPositions(floatArrayOf(0.1f, 0.5f, 0.9f))
        textView.setLinearGradientTileMode(Shader.TileMode.CLAMP)
    }

    private fun initIconAnim() {
        val iv = findViewById<ImageView>(R.id.lock_icon)
        iv.setOnClickListener {
            LockScreenService.disableSystemLockScreen(this)
            APKUtils.startApp(this@DebugLockActivity, "com.tencent.mm")
            finish()
        }
        val animatorSet = AnimatorSet()
        val animatorX = ObjectAnimator.ofFloat(iv, "scaleX", 1f, 1.2f, 1f)
        val animatorY = ObjectAnimator.ofFloat(iv, "scaleY", 1f, 1.2f, 1f)
        animatorX.repeatCount = -1
        animatorY.repeatCount = -1
        animatorSet.play(animatorX).with(animatorY)
        animatorSet.setDuration(1000).start()
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    override fun onBack() {}

    override fun finish() {
        super.finish()
        TaskManager.getInstance().removeTask(TASK_NAME)
        BatteryHelper.stopReceiveBatteryChanged(this, mBatteryChangedReceiver)
        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                val keyGuardService = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                val kl = keyGuardService.newKeyguardLock("unLock");
                //解锁
                kl.disableKeyguard();
            } catch (e: Exception) {
                ZLog.e("disableSystemLockScreen exception, cause: " + e.cause + ", message: " + e.message)
            }
        }
        WidgetUpdateManager.updateAllWidgets(this)
    }

    companion object {
        private const val TASK_NAME = "LockActivity"
    }
}