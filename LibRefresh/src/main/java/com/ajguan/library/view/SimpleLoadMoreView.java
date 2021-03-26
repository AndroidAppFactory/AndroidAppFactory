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
        tvHitText.setText("正在加载...");
    }

    @Override
    public void loading() {
        tvHitText.setVisibility(VISIBLE);
        tvHitText.setText("正在加载...");
    }

    @Override
    public void loadComplete() {
        tvHitText.setVisibility(VISIBLE);
        tvHitText.setText("加载完成");

    }

    @Override
    public void loadFail() {
        tvHitText.setVisibility(VISIBLE);
        tvHitText.setText("加载失败,点击重新加载");

    }

    @Override
    public void loadNothing() {
        tvHitText.setVisibility(VISIBLE);
        tvHitText.setText("没有更多可以加载");
    }

    @Override
    public View getCanClickFailView() {
        return view;
    }


}
