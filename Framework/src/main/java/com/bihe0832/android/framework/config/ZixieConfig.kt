package com.bihe0832.android.framework.config

import com.bihe0832.android.framework.ZixieContext.isOfficial
import com.bihe0832.android.lib.config.Config

object ZixieConfig {

    fun getConfigByKey(key: String?, official: String): String {
        return if (isOfficial()) {
            official
        } else {
            Config.readConfig(key, official)
        }
    }
}