/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/5/31 下午11:36
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/31 下午11:19
 *
 */
package com.bihe0832.android.lib.ace.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Pair;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.StringRes;

import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public class AceEditorView extends FrameLayout {


    public interface OnReadContentReadyListener {
        void onReadContentReady(byte[] content, String mimeType);

        void onContentUnchanged();
    }

    public interface OnContentChangedListener {
        void onContentChanged();
    }

    public interface OnMessageListener {
        void onErrorMessage(@StringRes int msg);

        void onWarnMessage(@StringRes int msg);
    }

    private final static int MAX_EXCHANGE_SIZE = 25000;

    private AceWebView mWebView;
    private final Random mRandom = new Random();

    private final Pattern mPattern = Pattern.compile("edt:(\\d)+:[pfm]:");

    private boolean mReadOnly = AceConstants.VALUE_LAST_IS_READ_ONLY;
    private boolean mWrap = AceConstants.VALUE_LAST_IS_AUTO_WRAP;
    private int mTextSize = AceConstants.VALUE_LAST_TEXT_SIZE;
    private boolean mNotifyMimeTypeChanges;
    private boolean mIsDirty;
    private boolean mInitialLoad = true;

    private boolean mReady;
    private boolean mIgnoreNextUnsavedEvent;
    private String mPendingFileName;
    private byte[] mPendingContent;
    private String mResolveMimetType = null;

    private OnContentChangedListener mContentChangedListener;
    private OnMessageListener mMessageListener;

    private SparseArray<Pair<StringBuilder, OnReadContentReadyListener>> mMap = new SparseArray<>();

    public AceEditorView(Context context) {
        this(context, null);
    }

    public AceEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AceEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(
                attrs, R.styleable.AceEditorView, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.AceEditorView_locked) {
                mReadOnly = a.getBoolean(attr, mReadOnly);
            }
            if (attr == R.styleable.AceEditorView_wrap) {
                mWrap = a.getBoolean(attr, mWrap);
            }
        }
        a.recycle();

        // Create the webview
        mWebView = createWebView();
        addView(mWebView);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private AceWebView createWebView() {
        AceWebView webview = new AceWebView(getContext());
        webview.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Editor is ready. Send configuration and content
                mReady = true;
                setReadOnly(mReadOnly);
                setWrap(mWrap);
                setTextSize(mTextSize);
                setNotifyMimeTypeChanges(mNotifyMimeTypeChanges);

                if (mPendingContent != null) {
                    loadEncodedContent(mPendingFileName, mPendingContent);
                    mPendingFileName = null;
                    mPendingContent = null;
                }
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                final String msg = consoleMessage.message();
                if (msg.equals("edt:crc")) {
                    if (mMessageListener != null) {
                        mMessageListener.onErrorMessage(R.string.ace_editor_load_failed);
                    }
                    return true;
                }
                if (msg.equals("edt:big")) {
                    if (mMessageListener != null) {
                        mMessageListener.onWarnMessage(R.string.ace_editor_file_too_big);
                    }
                    return true;
                }

                if (msg.equals("edt:u")) {
                    if (!mIgnoreNextUnsavedEvent) {
                        mIsDirty = true;
                        if (mContentChangedListener != null) {
                            mContentChangedListener.onContentChanged();
                        }
                    }
                    mIgnoreNextUnsavedEvent = false;
                    return true;
                }

                if (msg.startsWith("edt:mtc:")) {
                    String mimeType = msg.substring(msg.lastIndexOf(":") + 1);
                    if (TextUtils.isEmpty(mimeType)) {
                        mimeType = "text/plain";
                    }
                    String ext = AceEditorView.resolveExtensionFromMimeType(mimeType);
                    if (mReady) {
                        mWebView.loadUrl("javascript: setFileName('unnamed." + ext + "');");
                    }
                }

                if (mPattern.matcher(msg).find()) {
                    String[] v = msg.split(":");
                    int key = Integer.parseInt(v[1]);
                    final Pair<StringBuilder, OnReadContentReadyListener> o = mMap.get(key);
                    if (o == null) {
                        super.onConsoleMessage(consoleMessage);
                        return false;
                    }

                    // Read the message
                    switch (v[2]) {
                        case "p":
                            // partial
                            o.first.append(v[3]);
                            break;
                        case "m":
                            mResolveMimetType = v[3];
                            break;
                        case "f":
                            // finish
                            byte[] content;
                            if (o.first.length() == 0) {
                                content = new byte[0];
                            } else {
                                content = o.first.toString().getBytes();
                            }

                            int crc = Integer.parseInt(v[3]);
                            int computedCrc = crc(content);
                            if (crc != -1 && crc != computedCrc) {
                                if (mMessageListener != null) {
                                    mMessageListener.onErrorMessage(R.string.ace_editor_save_failed);
                                }
                                return true;
                            }
                            o.second.onReadContentReady(content, mResolveMimetType);
                            mMap.remove(key);
                            break;
                    }
                    return true;
                }

                super.onConsoleMessage(consoleMessage);
                return false;
            }
        });
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        String url = String.format(Locale.US,
                "file:///android_asset/editor/editor.html?cache=%d&enable-selection-handles=%s",
                System.currentTimeMillis(),
                (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.JELLY_BEAN));
        webview.loadUrl(url);

        return webview;
    }

    public void setDebug(boolean isDebug) {
        if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(isDebug);
        }
    }

    public boolean isWrap() {
        return mWrap;
    }

    public AceEditorView setWrap(boolean wrap) {
        mWrap = wrap;
        if (mReady) {
            mWebView.loadUrl("javascript: setWrapMode(" + mWrap + ");");
        }
        return this;
    }

    public boolean isReadOnly() {
        return mReadOnly;
    }

    public AceEditorView setReadOnly(boolean readOnly) {
        mReadOnly = readOnly;
        mWebView.setReadOnly(readOnly);
        if (mReady) {
            mWebView.loadUrl("javascript: setReadOnly(" + mReadOnly + ");");
        }
        return this;
    }


    public int getTextSize() {
        return mTextSize;
    }

    public AceEditorView setTextSize(int textSize) {
        mTextSize = textSize;
        if (mReady) {
            mWebView.loadUrl("javascript: setTextSize(" + mTextSize + ");");
        }
        return this;
    }

    public boolean isNotifyMimeTypeChanges() {
        return mNotifyMimeTypeChanges;
    }

    public AceEditorView setNotifyMimeTypeChanges(boolean notify) {
        mNotifyMimeTypeChanges = notify;
        if (mReady) {
            mWebView.loadUrl("javascript: setNotifyMimeTypeChanges(" + notify + ");");
        }
        return this;
    }

    public boolean isDirty() {
        return mIsDirty;
    }

    public AceEditorView listenOn(OnContentChangedListener cb) {
        mContentChangedListener = cb;
        return this;
    }

    public AceEditorView listenOn(OnMessageListener cb) {
        mMessageListener = cb;
        return this;
    }

    public void loadContent(String fileName, byte[] content) {
        loadEncodedContent(fileName, Base64.encode(content, Base64.NO_WRAP));
    }

    public void loadEncodedContent(String fileName, byte[] encoded) {
        // Enqueue the request
        if (!mReady) {
            mPendingFileName = fileName;
            mPendingContent = encoded;
            return;
        }

        int crc = 0;
        if (encoded.length > 0) {
            int s = 0;
            do {
                int e = Math.min(encoded.length - s, MAX_EXCHANGE_SIZE);
                String msg = new String(encoded, s, e);
                mWebView.loadUrl("javascript: addPartialContent('" + msg + "');");
                s += e;
            } while (s < encoded.length);
            crc = crc(encoded);
        }
        mIgnoreNextUnsavedEvent = true;
        if (!mInitialLoad) {
            mIsDirty = false;
        }
        mInitialLoad = false;
        mWebView.loadUrl("javascript: loadContent('" + fileName + "','" + crc + "');");
    }

    public void readContent(OnReadContentReadyListener cb) {
        readContent(cb, false);
    }

    public void readContent(OnReadContentReadyListener cb, boolean resolveMimeType) {
        if (mIsDirty) {
            int key = mRandom.nextInt(Short.MAX_VALUE);
            mMap.put(key, new Pair<>(new StringBuilder(), cb));
            mWebView.loadUrl("javascript: readContent(" + resolveMimeType + "," + key + ");");
        } else {
            cb.onContentUnchanged();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mReadOnly = mReadOnly;
        savedState.mWrap = mWrap;
        savedState.mTextSize = mTextSize;
        savedState.mIsDirty = mIsDirty;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mReadOnly = savedState.mReadOnly;
        mWrap = savedState.mWrap;
        mTextSize = savedState.mTextSize;
        mIsDirty = savedState.mIsDirty;
    }

    private static int crc(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return (int) crc.getValue();
    }

    public static String resolveExtensionFromMimeType(String mimeType) {
        if (mimeType != null) {
            switch (mimeType) {
                case "text/javascript":
                    return "js";
                case "text/x-c":
                    return "c";
                case "text/x-c++":
                    return "cpp";
                case "text/x-python":
                    return "py";
                case "text/x-java":
                    return "java";
                case "text/html":
                    return "html";
                case "text/css":
                    return "css";
                case "text/x-ruby":
                    return "rb";
                case "text/x-go":
                    return "go";
                case "text/x-php":
                    return "php";
            }
        }
        return "txt";
    }

    private static class SavedState extends BaseSavedState {
        boolean mReadOnly;
        boolean mWrap;
        int mTextSize;
        boolean mIsDirty;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mReadOnly = in.readInt() == 1;
            mWrap = in.readInt() == 1;
            mTextSize = in.readInt();
            mIsDirty = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mReadOnly ? 1 : 0);
            out.writeInt(mWrap ? 1 : 0);
            out.writeInt(mTextSize);
            out.writeInt(mIsDirty ? 1 : 0);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
