package com.bihe0832.android.common.webview.base;

import android.content.Context;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SSL 错误对话框辅助类，统一处理 TBS 和 Native WebView 的 SSL 错误弹框逻辑。
 * <p>
 * 特性：
 * 1. 用户选择"继续"后，当前页面后续 SSL 错误自动 proceed，不再弹框
 * 2. 对话框显示期间，新的 SSL 错误 handler 加入等待队列，用户操作后统一处理
 * 3. 页面导航变化时可通过 {@link #resetForNewPage()} 重置状态
 * <p>
 * 注意：本类所有方法必须在主线程调用（与 WebViewClient 回调线程一致）。
 */
public class SslErrorDialogHelper {

    /**
     * 对 SslErrorHandler 的包装接口，屏蔽 TBS 和 Native 的类型差异
     */
    public interface SslErrorHandlerWrapper {
        void proceed();

        void cancel();
    }

    private static final String TAG = "SslErrorDialogHelper";

    // 用户是否已经选择过"继续"，选择后当前页面生命周期内不再弹框
    private boolean mSslUserProceeded = false;
    // 对话框是否正在显示
    private boolean mSslDialogShowing = false;
    // 对话框显示期间，收集等待中的 handler，用户点击后统一处理
    private final List<SslErrorHandlerWrapper> mPendingHandlers = new ArrayList<>();

    /**
     * 页面导航变化时重置状态，确保新页面的 SSL 错误会重新提示用户。
     * 建议在 onPageStarted 中调用。
     */
    public void resetForNewPage() {
        mSslUserProceeded = false;
        // 如果对话框正在显示，不主动关闭，让用户自行操作
    }

    /**
     * 完全重置所有状态（包括取消所有 pending handler）。
     * 适用于 WebView 销毁或重建场景。
     */
    public void reset() {
        mSslUserProceeded = false;
        mSslDialogShowing = false;
        for (SslErrorHandlerWrapper handler : mPendingHandlers) {
            handler.cancel();
        }
        mPendingHandlers.clear();
    }

    /**
     * 统一处理 SSL 错误
     *
     * @param context   用于显示对话框的 Context
     * @param tag       日志 TAG
     * @param url       触发 SSL 错误的 URL
     * @param errorInfo SSL 错误信息（用于日志）
     * @param handler   包装后的 SslErrorHandler
     */
    public void handleSslError(Context context, String tag, String url, String errorInfo,
                               SslErrorHandlerWrapper handler) {
        ZLog.info(tag, "onReceivedSslError: url=" + url + ", sslError=" + errorInfo);

        // 用户已经选择过"继续"，后续全部自动 proceed
        if (mSslUserProceeded) {
            ZLog.info(tag, "onReceivedSslError: auto proceed (user already accepted), url=" + url);
            handler.proceed();
            return;
        }

        // 对话框正在显示，将 handler 加入等待队列
        if (mSslDialogShowing) {
            ZLog.info(tag, "onReceivedSslError: dialog showing, queued handler, url=" + url);
            mPendingHandlers.add(handler);
            return;
        }

        // 首次触发，弹对话框
        mSslDialogShowing = true;
        mPendingHandlers.add(handler);

        DialogUtils.INSTANCE.showConfirmDialog(context,
                context.getResources().getString(com.bihe0832.android.model.res.R.string.dialog_title),
                context.getResources().getString(com.bihe0832.android.model.res.R.string.com_bihe0832_web_ssl_error_message),
                context.getResources().getString(com.bihe0832.android.model.res.R.string.dialog_button_ok),
                context.getResources().getString(com.bihe0832.android.model.res.R.string.dialog_button_cancel),
                new OnDialogListener() {
                    @Override
                    public void onPositiveClick() {
                        ZLog.info(TAG, "onReceivedSslError: user chose proceed, pending=" + mPendingHandlers.size());
                        mSslUserProceeded = true;
                        for (SslErrorHandlerWrapper h : mPendingHandlers) {
                            h.proceed();
                        }
                        mPendingHandlers.clear();
                        mSslDialogShowing = false;
                    }

                    @Override
                    public void onNegativeClick() {
                        ZLog.info(TAG, "onReceivedSslError: user chose cancel, pending=" + mPendingHandlers.size());
                        mSslDialogShowing = false;
                        for (SslErrorHandlerWrapper h : mPendingHandlers) {
                            h.cancel();
                        }
                        mPendingHandlers.clear();
                    }

                    @Override
                    public void onCancel() {
                        ZLog.info(TAG, "onReceivedSslError: dialog dismissed");
                        onNegativeClick();
                    }
                });
    }
}
