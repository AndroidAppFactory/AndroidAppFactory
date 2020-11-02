package com.bihe0832.android.test.module;

import android.os.Bundle;

import com.bihe0832.android.lib.router.Routers;
import com.bihe0832.android.test.base.BaseTestFragment;
import com.bihe0832.android.test.base.OnTestItemClickListener;
import com.bihe0832.android.test.base.TestItem;

import java.util.ArrayList;
import java.util.List;

public class TestRouterFragment extends BaseTestFragment {

    public static TestRouterFragment newInstance() {

        Bundle args = new Bundle();
        TestRouterFragment fragment = new TestRouterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public List<TestItem> getDataList() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new RouterItem("zixie://test"));
        items.add(new RouterItem("zixie://testhttp"));
        items.add(new RouterItem("zixie://feedback"));
        return items;
    }

    private class RouterItem extends TestItem{

        public RouterItem(final String url){
            super(url, new OnTestItemClickListener() {
                @Override
                public void onItemClick(TestItem testItem) {
                    Routers.open(getContext(),url);
                }
            });

        }
    }
}