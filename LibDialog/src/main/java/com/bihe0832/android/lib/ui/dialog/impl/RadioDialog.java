package com.bihe0832.android.lib.ui.dialog.impl;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;

import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.ui.dialog.R;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zixie code@bihe0832.com Created on 7/20/21.
 */
public class RadioDialog extends CommonDialog {

    public RadioDialog(Context context) {
        super(context);
    }

    public RadioDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    private RadioGroup mRadioGroup = null;
    private List<String> mDataList = new ArrayList<>();
    private OnSelectedListener mOnSelectedListener = null;
    private int mCheckedIndex = -1;

    public interface OnSelectedListener {

        public void onSelect(int which);
    }

    @Override
    protected void refreshView() {
        super.refreshView();
        if (null == mDataList || mDataList.isEmpty() || null == mRadioGroup) {
            return;
        }
        mRadioGroup.removeAllViews();
        for (int i = 0; i < mDataList.size(); i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setId(i);
//            radioButton.setChecked(i == mCheckedIndex);
            radioButton.setButtonDrawable(getContext().getResources().getDrawable(com.bihe0832.android.lib.aaf.res.R.drawable.com_bihe0832_base_radio_selctor));
            radioButton.setPadding(DisplayUtil.dip2px(getContext(), 8f), DisplayUtil.dip2px(getContext(), 8f), 0, DisplayUtil.dip2px(getContext(), 8f));
            //设置文字
            radioButton.setText(mDataList.get(i));
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(com.bihe0832.android.lib.aaf.res.R.dimen.com_bihe0832_dialog_content_text_size));
            radioButton.setTextColor(getContext().getResources().getColorStateList(com.bihe0832.android.lib.aaf.res.R.drawable.com_bihe0832_base_select_color));
            final int index = i;
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCheckedIndex = index;
                    if (null != mOnSelectedListener) {
                        mOnSelectedListener.onSelect(mCheckedIndex);
                    }
                }
            });
            mRadioGroup.addView(radioButton);
        }

        if (mCheckedIndex < mDataList.size() && null != mRadioGroup) {
            mRadioGroup.check(mCheckedIndex);
        }
    }

    @Override
    public void initView() {
        super.initView();
        mRadioGroup = new RadioGroup(getContext());
        LinearLayout.LayoutParams radioGroupLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        int margin = DisplayUtil.dip2px(getContext(), 16f);
        radioGroupLayoutParams.setMargins(0, margin / 2, 0, margin / 2);
        mRadioGroup.setLayoutParams(radioGroupLayoutParams);
        addViewToContent(mRadioGroup);

    }

    public int getCheckedIndex() {
        return mCheckedIndex;
    }

    @Override
    public void show() {
        super.show();
    }

    public void setRadioData(List<String> data, int index, final OnSelectedListener listener) {
        mDataList.clear();
        mCheckedIndex = index;
        mDataList.addAll(data);
        mOnSelectedListener = listener;
    }
}
