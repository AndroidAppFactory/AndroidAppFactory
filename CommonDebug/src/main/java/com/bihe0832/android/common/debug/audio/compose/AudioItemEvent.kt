package com.bihe0832.android.common.debug.audio.compose

import com.bihe0832.android.common.compose.mvi.ViewEvent

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/3.
 * Description: Description
 *
 */
sealed class AudioItemEvent : ViewEvent {
    object IconClick : AudioItemEvent()
}