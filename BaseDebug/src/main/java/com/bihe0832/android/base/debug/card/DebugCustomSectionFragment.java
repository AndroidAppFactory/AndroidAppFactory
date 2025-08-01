package com.bihe0832.android.base.debug.card;


import android.view.View;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.base.debug.card.section.SectionDataContentTest;
import com.bihe0832.android.base.debug.card.section.SectionDataHeader2;
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

        for (int i = 0; i < 6; i++) {
            CardBaseModule sectionHeader = null;
            sectionHeader = new SectionDataHeader2("标题2:" + i);
            mDataList.add(sectionHeader);
            for (int j = 0; j < 15; j++) {
                CardBaseModule section;
                section = new SectionDataContentTest("内容2:" + j);
                mDataList.add(section);
            }
        }
    }
}
