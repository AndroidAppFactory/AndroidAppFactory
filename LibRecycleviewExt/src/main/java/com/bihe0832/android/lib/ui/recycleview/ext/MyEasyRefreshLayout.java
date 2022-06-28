package com.bihe0832.android.lib.ui.recycleview.ext;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ajguan.library.EasyRefreshLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;


/**
 * @author zixie code@bihe0832.com
 * Created on 2019-09-25.
 * Description: Description
 */
public class MyEasyRefreshLayout extends EasyRefreshLayout {

    public MyEasyRefreshLayout(Context context) {
        this(context, null);

    }

    public MyEasyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canChildScrollUp() {
        if (getContentView() instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) getContentView();
            final RecyclerView.Adapter adapter = recyclerView.getAdapter();
            int headerNum = 0;
            if(adapter instanceof BaseQuickAdapter){
                headerNum = ((BaseQuickAdapter) adapter).getHeaderLayoutCount();
                return recyclerView.getChildCount() > 0
                        && (((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > headerNum || recyclerView.getChildAt(0)
                        .getTop() < recyclerView.getPaddingTop());
            }
        }

        return super.canChildScrollUp();
    }
}
