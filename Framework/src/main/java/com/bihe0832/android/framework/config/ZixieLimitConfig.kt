package com.bihe0832.android.framework.config

import com.bihe0832.android.framework.ZixieContext.isOfficial
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.utils.time.DateUtil

/**
 * 限频配置管理类
 * 记录间隔 interal 天（0表示当天），最多 limitTime 次的任务记录
 *
 * @author zixie code@bihe0832.com
 */
object ZixieLimitConfig {

    private const val KEY_LAST_SHOW_TIME = "KEY_FIRST_USE_TIME" // 最后显示时间
    private const val KEY_USED_TIMES = "KEY_USED_TIMES" // 已使用次数

    /**
     * 根据key获取配置值，官方版本使用固定值，非官方版本可配置
     *
     * @param key 配置键，可为空
     * @param officialValue 默认值（官方版本使用此值）
     * @return 配置值
     */
    fun getConfigByKey(key: String?, officialValue: String): String {
        return if (isOfficial()) {
            officialValue
        } else {
            Config.readConfig(key, officialValue)
        }
    }

    /**
     * 检查是否需要重置次数（超过时间间隔或不是同一天）
     *
     * @param lastShowTime 最后显示时间
     * @param curTime 当前时间
     * @param interval 间隔天数（小于1表示当天，1表示跨1天，以此类推）
     * @param isNaturalDay 是否按自然日计算
     *   - true: 按自然日跨天计算（例如：1月1日23:59到1月2日00:01算跨1天）
     *   - false: 按时间间隔计算（例如：必须满24小时才算跨1天）
     * @return true表示需要重置，false表示不需要
     */
    private fun shouldResetTimes(lastShowTime: Long, curTime: Long, interval: Int, isNaturalDay: Boolean): Boolean {
        return if (interval < 1) {
            // interval<1 表示当天，不区分模式（包括0和负数等异常值）
            !DateUtil.isToday(lastShowTime)
        } else {
            // interval>=1 表示跨N天
            if (isNaturalDay) {
                // 自然日模式：从 lastShowTime 所在那天的 00:00:00 开始计算
                val startTime = DateUtil.getDayStartTimestamp(lastShowTime)
                curTime - startTime >= interval * DateUtil.MILLISECOND_OF_DAY
            } else {
                // 时间间隔模式：从 lastShowTime 开始计算，必须满足完整的时间间隔
                curTime - lastShowTime >= interval * DateUtil.MILLISECOND_OF_DAY
            }
        }
    }

    /**
     * 获取当前剩余次数
     *
     * @param sceneId 场景ID
     * @param interval 间隔天数（0表示当天）
     * @param limitTime 限制次数
     * @param isNaturalDay 是否按自然日计算，默认false
     * @return 当前剩余次数
     */
    @JvmOverloads
    fun getCanUseTimes(sceneId: String, interval: Int, limitTime: Int, isNaturalDay: Boolean = false): Int {
        val lastShowTime = Config.readConfig(KEY_LAST_SHOW_TIME + sceneId, 0L)
        val usedTimes = Config.readConfig(KEY_USED_TIMES + sceneId, 0)
        val curTime = System.currentTimeMillis()
        return if (shouldResetTimes(lastShowTime, curTime, interval, isNaturalDay)) {
            limitTime
        } else {
            limitTime - usedTimes
        }
    }

    /**
     * 检查是否还有剩余次数
     *
     * @param sceneId 场景ID
     * @param interval 间隔天数（0表示当天）
     * @param limitTime 限制次数
     * @param isNaturalDay 是否按自然日计算，默认false
     * @return true表示还有次数，false表示已用完
     */
    @JvmOverloads
    fun hasTimes(sceneId: String, interval: Int, limitTime: Int, isNaturalDay: Boolean = false): Boolean {
        val usedTimes = getCanUseTimes(sceneId, interval, limitTime, isNaturalDay)
        return usedTimes > 0
    }

    /**
     * 消耗一次次数
     *
     * @param sceneId 场景ID
     * @param interval 间隔天数（0表示当天）
     * @param isNaturalDay 是否按自然日计算，默认false
     */
    @JvmOverloads
    fun costTimes(sceneId: String, interval: Int, isNaturalDay: Boolean = false) {
        val lastShowTime = Config.readConfig(KEY_LAST_SHOW_TIME + sceneId, 0L)
        val usedTimes = Config.readConfig(KEY_USED_TIMES + sceneId, 0)
        val curTime = System.currentTimeMillis()
        if (shouldResetTimes(lastShowTime, curTime, interval, isNaturalDay)) {
            Config.writeConfig(KEY_LAST_SHOW_TIME + sceneId, System.currentTimeMillis())
            Config.writeConfig(KEY_USED_TIMES + sceneId, 1)
        } else {
            Config.writeConfig(KEY_USED_TIMES + sceneId, usedTimes + 1)
        }
    }
}
