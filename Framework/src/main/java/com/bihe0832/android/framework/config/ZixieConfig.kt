package com.bihe0832.android.framework.config

import com.bihe0832.android.framework.ZixieContext.isOfficial
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.utils.time.DateUtil

object ZixieConfig {

    private const val KEY_TRACE_TIME = "KEY_FIRST_USE_TIME" // 时间
    private const val KEY_TRACE_TIMES = "KEY_USED_TIMES" // 次数

    fun getConfigByKey(key: String?, official: String): String {
        return if (isOfficial()) {
            official
        } else {
            Config.readConfig(key, official)
        }
    }

    /**
     * 间隔 interal 天（0表示当天），最多 limitTime 次
     */
    fun hasTimes(sceneId: String, interal: Int, limitTime: Int): Boolean {
        val lastShowTime = Config.readConfig(KEY_TRACE_TIME + sceneId, 0L)
        val hasShowTimes = Config.readConfig(KEY_TRACE_TIMES + sceneId, 0)
        val curTime = System.currentTimeMillis()
        return if (interal > 0) {
            if (curTime - lastShowTime > interal * DateUtil.MILLISECOND_OF_DAY) {
                true
            } else {
                hasShowTimes < limitTime
            }
        } else {
            if (DateUtil.isToady(lastShowTime)) {
                hasShowTimes < limitTime
            } else {
                true
            }
        }
        return false
    }

    fun costTimes(sceneId: String, interal: Int) {
        val lastShowTime = Config.readConfig(KEY_TRACE_TIME + sceneId, 0L)
        val hasShowTimes = Config.readConfig(KEY_TRACE_TIMES + sceneId, 0)
        val curTime = System.currentTimeMillis()
        if (interal > 0) {
            if (curTime - lastShowTime > interal * DateUtil.MILLISECOND_OF_DAY) {
                Config.writeConfig(KEY_TRACE_TIME + sceneId, System.currentTimeMillis())
                Config.writeConfig(KEY_TRACE_TIMES + sceneId, 1)
            } else {
                Config.writeConfig(KEY_TRACE_TIMES + sceneId, lastShowTime + 1)
            }
        } else {
            if (DateUtil.isToady(lastShowTime)) {
                Config.writeConfig(KEY_TRACE_TIMES + sceneId, lastShowTime + 1)
            } else {
                Config.writeConfig(KEY_TRACE_TIME + sceneId, System.currentTimeMillis())
                Config.writeConfig(KEY_TRACE_TIMES + sceneId, 1)
            }
        }
    }
}
