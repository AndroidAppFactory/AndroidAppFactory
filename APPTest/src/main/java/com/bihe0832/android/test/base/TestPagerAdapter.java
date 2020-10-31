package com.bihe0832.android.test.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bihe0832.android.test.R;

import java.util.ArrayList;
import java.util.List;


public class TestPagerAdapter extends RecyclerView.Adapter<TestPagerAdapter.MyViewHolder> {
    private List<TestItem> mItems = new ArrayList<>();
    private LayoutInflater mInflater;

    public TestPagerAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setDatas(List<TestItem> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.test_item_pager, parent, false);
        final MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final TestItem item = mItems.get(position);
        holder.tv_title.setText(Html.fromHtml(item.mTitle));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                mItems.get(position).mListener.onItemClick(item);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    protected class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_title;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.test_title);
        }
    }
}
