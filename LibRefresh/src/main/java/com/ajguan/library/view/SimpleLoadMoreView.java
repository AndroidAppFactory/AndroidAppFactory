package com.ajguan.library.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ajguan.library.ILoadMoreView;
import com.bihe0832.android.lib.refresh.R;



public class SimpleLoadMoreView extends FrameLayout implements ILoadMoreView {

    private TextView tvHitText;
    private View view;

    public SimpleLoadMoreView(Context context) {
        this(context, null);
    }

    public SimpleLoadMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        view = inflate(context, R.layout.default_load_more, this);
        tvHitText = (TextView) view.findViewById(R.id.tv_hit_content);

    }


    @Override
    public void reset() {
        tvHitText.setVisibility(INVISIBLE);
        tvHitText.setText(getContext().getString(com.bihe0832.android.lib.aaf.res.R.string.com_bihe0832_loading));
    }

    @Override
    public void loading() {
        tvHitText.setVisibility(VISIBLE);
        tvHitText.setText(getContext().getString(com.bihe0832.android.lib.aaf.res.R.string.com_bihe0832_loading));
    }

    @Override
    public void loadComplete() {
        tvHitText.setVisibility(VISIBLE);
        tvHitText.setText(getContext().getString(com.bihe0832.android.lib.aaf.res.R.string.com_bihe0832_loading_completed));

    }

    @Override
    public void loadFail() {
        tvHitText.setVisibility(VISIBLE);
        tvHitText.setText(getContext().getString(com.bihe0832.android.lib.aaf.res.R.string.com_bihe0832_load_failed));

    }

    @Override
    public void loadNothing() {
        tvHitText.setVisibility(VISIBLE);
        tvHitText.setText(getContext().getString(com.bihe0832.android.lib.aaf.res.R.string.com_bihe0832_load_more_end));
    }

    @Override
    public View getCanClickFailView() {
        return view;
    }


}
