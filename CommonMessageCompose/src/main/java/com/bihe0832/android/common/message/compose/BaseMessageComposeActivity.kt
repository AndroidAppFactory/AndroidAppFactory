package com.bihe0832.android.common.message.compose

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.state.aafStringResource
import com.bihe0832.android.common.compose.ui.EmptyView
import com.bihe0832.android.common.message.base.MessageManager
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.model.res.R as ModelResR

/**
 * 消息列表 Compose Activity 基类
 *
 * 对应 View 体系中的 CommonActivity + BaseMessageFragment。
 * 继承 CommonComposeActivity，自动拥有 Toolbar + 主题 + 返回键处理。
 * 子类需实现 [getMessageManager] 和 [showMessage] 方法。
 *
 * @author zixie code@bihe0832.com
 * Created on 2026/3/26.
 */
abstract class BaseMessageComposeActivity : CommonComposeActivity() {

    /**
     * 获取消息管理器
     */
    abstract fun getMessageManager(): MessageManager

    /**
     * 显示消息详情
     *
     * @param activity 当前 Activity
     * @param messageInfoItem 消息数据
     * @param showFace 是否以拍脸方式显示
     */
    abstract fun showMessage(activity: Activity, messageInfoItem: MessageInfoItem, showFace: Boolean)

    @Composable
    override fun getTitleName(): String {
        return aafStringResource(ModelResR.string.settings_message_title)
    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                MessageListContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getMessageManager().updateMsg()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MessageListContent() {
        val lifecycleOwner = LocalLifecycleOwner.current
        val messageManager = getMessageManager()

        // 观察消息 LiveData
        var messageList by remember { mutableStateOf<List<MessageInfoItem>>(emptyList()) }
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<List<MessageInfoItem>?> { list ->
                messageList = list?.filter { !it.hasDelete() } ?: emptyList()
            }
            messageManager.getMessageLiveData().observe(lifecycleOwner, observer)
            onDispose {
                messageManager.getMessageLiveData().removeObserver(observer)
            }
        }

        val isRefreshing by remember { mutableStateOf(false) }
        val pullRefreshState = rememberPullToRefreshState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .pullToRefresh(
                    isRefreshing = isRefreshing,
                    state = pullRefreshState,
                    onRefresh = {
                        messageManager.updateMsg()
                    }
                )
        ) {
            if (messageList.isEmpty()) {
                // 空状态
                EmptyView(
                    message = aafStringResource(ModelResR.string.com_bihe0832_message_empty_text),
                    colorP = MaterialTheme.colorScheme.surface,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
            } else {
                // 消息列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = messageList,
                        key = { it.messageID ?: it.hashCode().toString() }
                    ) { messageItem ->
                        MessageItemCompose(
                            messageInfoItem = messageItem,
                            onItemClick = { item ->
                                item.setHasRead(true)
                                showMessage(this@BaseMessageComposeActivity, item, false)
                                // 触发列表刷新
                                messageManager.updateMsg()
                            },
                            onDelete = { item ->
                                messageManager.deleteMessage(item)
                                messageManager.updateMsg()
                            }
                        )
                    }
                }
            }
        }
    }
}
