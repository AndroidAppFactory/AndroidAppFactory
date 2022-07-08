package com.bihe0832.android.base.debug.card;

import static com.bihe0832.android.base.debug.card.DebugListActivityKt.ROUTRT_NAME_TEST_SECTION;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bihe0832.android.app.router.RouterHelper;
import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.base.debug.card.section.SectionDataContent2;
import com.bihe0832.android.base.debug.card.section.SectionDataHeader2;
import com.bihe0832.android.common.debug.item.DebugTipsData;
import com.bihe0832.android.common.debug.log.SectionDataContent;
import com.bihe0832.android.common.debug.log.SectionDataHeader;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;


public class DebugCustomSectionFragment extends BaseFragment {

    private static final String TAG = "TestSectionFragment-> ";

    private DebugSectionAdapterForCustom mRecycleAdapter;
    private RecyclerView mRecycleView;
    private ArrayList<CardBaseModule> mDataList = new ArrayList<>();

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test_card;
    }

    @Override
    protected void initView(View mView) {
        super.initView(mView);
        initList();
        mRecycleView = (RecyclerView) mView.findViewById(R.id.card_list);
        mRecycleAdapter = new DebugSectionAdapterForCustom(this.getContext(), mDataList);
        mRecycleView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        mRecycleView.setAdapter(mRecycleAdapter);
        mRecycleAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {

            }
        });
    }

    private void initList() {

        mDataList.add(
                new DebugTipsData("点击打开List 测试Activity", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RouterHelper.INSTANCE.openPageByRouter(ROUTRT_NAME_TEST_SECTION);
                    }
                }));
        for (int i = 0; i < 6; i++) {
            CardBaseModule sectionHeader = null;
            if (i < 2) {
                sectionHeader = new SectionDataHeader("标题1:" + i);
            } else {
                sectionHeader = new SectionDataHeader2("标题2:" + i);
            }
            mDataList.add(sectionHeader);
            for (int j = 0; j < 15; j++) {
                CardBaseModule section;
                if (i < 2) {
                    section = new SectionDataContent("内容1:" + j, "");
                } else {
                    section = new SectionDataContent2("内容2:" + j);
                }
                mDataList.add(section);
            }
        }
    }
}
