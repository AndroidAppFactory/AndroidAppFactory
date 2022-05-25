/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 下午5:07
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/25 下午4:16
 *
 */

package com.bihe0832.android.lib.ui.textview.expandable;

import android.content.Context;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;

import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.ui.textview.ext.TextViewExtKt;


public class TextViewWithHTML extends android.support.v7.widget.AppCompatTextView {
    public TextViewWithHTML(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewWithHTML(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence htmlText, BufferType type) {
        Spanned html = TextFactoryUtils.getSpannedTextByHtml(htmlText.toString());
        ClickableSpan[] spans = html.getSpans(0, html.length(), ClickableSpan.class);
        if (spans != null && spans.length > 0) {
            setMovementMethod(LinkMovementMethod.getInstance());
        }
        super.setText(html, type);
    }

    public void setHTMLText(CharSequence htmlText) {
        setText(htmlText, BufferType.NORMAL);
    }


    public void setFormatText(String format, Object... args) {
        TextViewExtKt.setText(this, format, args);
    }
}
