package com.bihe0832.android.test.module.card;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.test.R;
import com.bihe0832.android.test.module.card.section.SectionDataContent;
import com.bihe0832.android.test.module.card.section.SectionDataContent2;
import com.bihe0832.android.test.module.card.section.SectionDataHeader;
import com.bihe0832.android.test.module.card.section.SectionDataHeader2;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;


public class TestSectionFragment extends BaseFragment {
    private static final String TAG = "TestSectionFragment-> ";

    private TestSectionAdapter mRecycleAdapter;
    private RecyclerView mRecycleView;
    private ArrayList<CardBaseModule> mDataList = new ArrayList<>();

    public static TestSectionFragment newInstance() {

        Bundle args = new Bundle();
        TestSectionFragment fragment = new TestSectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.activity_test_card, container, false);

        initList();
        initView(mView);
        return mView;
    }

    void initView(View mView){
        mRecycleView = (RecyclerView) mView.findViewById(R.id.card_list);
        mRecycleAdapter = new TestSectionAdapter(this.getContext(), mDataList);
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
            if(i < 2 ){
                sectionHeader = new SectionDataHeader("标题1:" + i);
            }else{
                sectionHeader = new SectionDataHeader2( "标题2:" + i);
            }
            mDataList.add(sectionHeader);
            for (int j = 0; j < 15; j++) {
                CardBaseModule section;
                if(i < 2 ){
                    section = new SectionDataContent( "内容1:" + j);
                }else{
                    section = new SectionDataContent2( "内容2:" + j);
                }
                mDataList.add(section);
            }
        }
    }
}
