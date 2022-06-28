package com.bihe0832.android.lib.ui.textview.span;

import android.graphics.Color;
import android.text.TextPaint;
import android.view.View;
import android.widget.TextView;

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/3/22.
 * Description: Description
 */
public class ZixieTextClickableSpan extends android.text.style.ClickableSpan {

    private View.OnClickListener mClicklistener = null;

    public ZixieTextClickableSpan(View.OnClickListener listener) {
        this.mClicklistener = listener;

    }

    @Override

    public void updateDrawState(TextPaint ds) {

    }

    @Override
    public void onClick(View widget) {

        if (widget instanceof TextView) {
            ((TextView) widget).setHighlightColor(Color.TRANSPARENT);
        }
        if (null != mClicklistener) {
            mClicklistener.onClick(widget);
        }
    }

}
