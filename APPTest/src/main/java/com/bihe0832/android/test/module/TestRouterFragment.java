package com.bihe0832.android.test.module;

import android.os.Bundle;

import com.bihe0832.android.lib.router.Routers;
import com.bihe0832.android.test.base.BaseTestFragment;
import com.bihe0832.android.test.base.OnTestItemClickListener;
import com.bihe0832.android.test.base.TestItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TestRouterFragment extends BaseTestFragment {

    public static TestRouterFragment newInstance() {

        Bundle args = new Bundle();
        TestRouterFragment fragment = new TestRouterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NotNull
    @Override
    public String getTipsText() {
        return "可以使用下面的工具测试：\nhttps://microdemo.bihe0832.com/MyJS/router/index.html ";
    }

    @Override
    public List<TestItem> getDataList() {
        List<TestItem> items = new ArrayList<TestItem>();
        items.add(new RouterItem("zixie://test"));
        items.add(new RouterItem("zixie://testhttp"));
        items.add(new RouterItem("zixie://feedback"));
        items.add(new RouterItem("zixie://web?url=https%3A%2F%2Fblog.bihe0832.com"));
        items.add(new RouterItem("zixie://feedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858"));
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