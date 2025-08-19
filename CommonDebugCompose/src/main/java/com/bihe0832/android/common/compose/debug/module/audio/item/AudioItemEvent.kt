package com.bihe0832.android.common.compose.debug.module.audio.item

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
    object IconLongClick : AudioItemEvent()
    object ContentClick : AudioItemEvent()
    object ContentLongClick : AudioItemEvent()
}