package com.bihe0832.android.base.compose.debug.ui

import com.bihe0832.android.app.ui.AAFCommonMainActivity

/**
 * 继承 AAFCommonMainActivity 的示例主页
 *
 * 演示如何基于 AAFCommonMainActivity 构建一个带侧边栏导航、标题栏操作按钮的主页。
 * AAFCommonMainActivity 已内置：
 * - 侧边栏导航（getDrawerContentContentRender）
 * - 标题栏右侧扫码 + 消息中心按钮（getTitleActionContentRender）
 * - 消息拍脸展示
 * - 版本更新检查
 *
 * 子类只需关注内容区域（getContentRender）的实现即可。
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 */
class SampleMainActivity : AAFCommonMainActivity() {

}
