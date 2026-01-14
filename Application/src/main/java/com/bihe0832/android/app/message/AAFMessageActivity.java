package com.bihe0832.android.app.message;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bihe0832.android.app.router.RouterConstants;
import com.bihe0832.android.framework.ui.main.CommonActivity;
import com.bihe0832.android.lib.router.annotation.Module;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;

/**
 * AAF 消息中心页面
 *
 * 展示应用内消息列表，支持消息的查看和管理
 * 通过路由 {@link RouterConstants#MODULE_NAME_MESSAGE} 访问
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
@Module(RouterConstants.MODULE_NAME_MESSAGE)
public final class AAFMessageActivity extends CommonActivity {

    /**
     * 页面创建时初始化
     *
     * 设置标题栏并加载消息列表 Fragment
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar(ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.model.res.R.string.settings_message_title), true);
        if (findFragment(AAFMessageListFragment.class) == null) {
            loadRootFragment(new AAFMessageListFragment());
        }
    }

    /**
     * 页面恢复时刷新消息列表
     */
    protected void onResume() {
        super.onResume();
        try {
            if (this.findFragment(AAFMessageListFragment.class) != null) {
                this.findFragment(AAFMessageListFragment.class).setUserVisibleHint(true);
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }
}
