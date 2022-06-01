/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/1 下午2:55
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/1 下午2:54
 *
 */
package com.bihe0832.android.lib.ace.poplist;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bihe0832.android.lib.ace.editor.R;

import java.util.ArrayList;
import java.util.List;

public class AceSelectionActionModeHelper {
    public interface OnSelectionItemPressed {
        boolean onSelectionItemPressed(int itemId, String label);
    }

    public static final int OPTION_CUT = 0;
    public static final int OPTION_COPY = 1;
    public static final int OPTION_PASTE = 2;
    public static final int OPTION_SELECT_ALL = 3;
    public static final int OPTION_SEARCH = 4;

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final ClipboardManager mClipboard;
    private final DisplayMetrics mMetrics;
    private PopupWindow mPopup;

    private int mInitialX;
    private int mInitialY;
    private int mPrimaryWidth;
    private int mPrimaryHeight;
    private int mSecondaryWidth;
    private int mSecondaryHeight;

    private ViewGroup mPrimaryOptionsViewBlock;
    private ViewGroup mSecondaryOptionsViewUpperBlock;
    private ViewGroup mSecondaryOptionsViewLowerBlock;
    private ViewGroup mPrimaryOptionsView;
    private ViewGroup mSecondaryOptionsUpperView;
    private ViewGroup mSecondaryOptionsLowerView;
    private View mOverflow;

    private OnSelectionItemPressed mOnSelectionItemPressedListener;
    private final List<Pair<String, Intent>> mOptions = new ArrayList<>();
    private final int basicOptionsLength;

    private boolean mHasSelection;
    private boolean mReadOnly;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnSelectionItemPressedListener != null) {
                final int itemId = (int) v.getTag();
                if (mOnSelectionItemPressedListener.onSelectionItemPressed(
                        itemId, mOptions.get(itemId).first)) {
                    dismiss();
                }
            }
        }
    };

    public AceSelectionActionModeHelper(@NonNull Context context, String[] options) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mClipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        mMetrics = context.getResources().getDisplayMetrics();

        // Add options
        basicOptionsLength = options.length;
        for (String option : options) {
            mOptions.add(new Pair<String, Intent>(option, null));
        }
    }

    public void listenOn(OnSelectionItemPressed cb) {
        mOnSelectionItemPressedListener = cb;
    }

    public void dismiss() {
        if (mPopup != null) {
            mPopup.dismiss();
            mPopup = null;
        }
    }

    public boolean isShowing() {
        return mPopup != null && mPopup.isShowing();
//        return mPopList != null && mPopList.s
    }

    public int getDefaultHeight() {
        return (int) mMetrics.density * 64;
    }

    public void show(View anchor) {
        int[] xy = new int[2];
        anchor.getLocationOnScreen(xy);
        show(anchor, xy[0], xy[1] + anchor.getHeight());
    }

    public void show(View anchor, int x, int y) {
        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
        }

        mInitialX = x;
        mInitialY = y;
        createPopUp();
        mPopup.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
    }

    public void hasSelection(boolean hasSelection) {
        mHasSelection = hasSelection;
    }

    public void setReadOnly(boolean readOnly) {
        mReadOnly = readOnly;
    }

    @SuppressLint("InflateParams")
    private void createPopUp() {
        ViewGroup contentView = (ViewGroup) mLayoutInflater.inflate(
                R.layout.ace_poplist_layout, null, false);
        mPrimaryOptionsViewBlock =
                contentView.findViewById(R.id.ace_selection_primary_options_block);
        mSecondaryOptionsViewUpperBlock =
                contentView.findViewById(R.id.ace_selection_secondary_options_upper_block);
        mSecondaryOptionsViewLowerBlock =
                contentView.findViewById(R.id.ace_selection_secondary_options_lower_block);
        mPrimaryOptionsView = contentView.findViewById(R.id.ace_selection_primary_options);
        mSecondaryOptionsUpperView =
                contentView.findViewById(R.id.ace_selection_secondary_upper_options);
        mSecondaryOptionsLowerView =
                contentView.findViewById(R.id.ace_selection_secondary_lower_options);
        mOverflow = contentView.findViewById(R.id.ace_selection_overflow);
        mOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowMoreOptions();
            }
        });
        View backUpper = contentView.findViewById(R.id.ace_selection_back_upper);
        backUpper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onHideMoreOptions();
            }
        });
        View backLower = contentView.findViewById(R.id.ace_selection_back_lower);
        backLower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onHideMoreOptions();
            }
        });
        createOptions();
        mSecondaryOptionsViewUpperBlock.setVisibility(View.GONE);
        mSecondaryOptionsViewLowerBlock.setVisibility(View.GONE);

        mPopup = new PopupWindow(contentView, WRAP_CONTENT, WRAP_CONTENT, false);
        mPopup.setBackgroundDrawable(ContextCompat.getDrawable(
                mContext, R.drawable.ace_poplist_bg));
        mPopup.setOutsideTouchable(true);
        mPopup.setTouchable(true);
        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPopup.dismiss();
            }
        });
    }

    private void createOptions() {
        final int maxWidth = (int) Math.min(
                600 * mMetrics.density, (75 * mMetrics.widthPixels / 100));

        mPrimaryOptionsView.removeAllViews();
        mSecondaryOptionsUpperView.removeAllViews();
        mSecondaryOptionsLowerView.removeAllViews();
        mPrimaryWidth = mPrimaryHeight = mSecondaryWidth = mSecondaryHeight = 0;
        mSecondaryHeight = (int) mMetrics.density * 8;

        boolean hasRoom = true;
        boolean first = true;
        int count = mOptions.size();
        final boolean upper = isUpperBlock();
        for (int i = 0; i < count; i++) {
            if (!hasOption(i)) {
                continue;
            }

            View v = createOption(hasRoom, i, mOptions.get(i).first);
            int[] measuring = measureView(v);
            if (!first && maxWidth < (mPrimaryWidth + measuring[0])) {
                v = createOption(false, i, mOptions.get(i).first);
                measuring = measureView(v);
                hasRoom = false;
            }

            if (hasRoom) {
                mPrimaryWidth += measuring[0];
                mPrimaryHeight = Math.max(measuring[1], mPrimaryHeight);
            } else {
                mSecondaryWidth = Math.max(measuring[0], mSecondaryWidth);
                mSecondaryHeight += measuring[1];
            }

            ViewGroup parent = hasRoom
                    ? mPrimaryOptionsView
                    : upper ? mSecondaryOptionsUpperView : mSecondaryOptionsLowerView;
            parent.addView(v);

            first = false;
        }

        mOverflow.setVisibility(hasRoom ? View.GONE : View.VISIBLE);
    }

    private TextView createOption(boolean hasRoom, int id, String text) {
        final int layoutId = hasRoom
                ? R.layout.ace_poplist_primary : R.layout.ace_poplist_secondary;
        TextView view = (TextView) mLayoutInflater.inflate(layoutId, null, false);
        view.setText(text);
        view.setTag(id);
        view.setOnClickListener(mOnClickListener);
        return view;
    }

    private void onShowMoreOptions() {
        final boolean upper = isUpperBlock();
        mPrimaryWidth = mPrimaryOptionsViewBlock.getWidth();
        mPrimaryOptionsViewBlock.setVisibility(View.GONE);
        mSecondaryOptionsViewUpperBlock.setVisibility(upper ? View.VISIBLE : View.GONE);
        mSecondaryOptionsViewLowerBlock.setVisibility(upper ? View.GONE : View.VISIBLE);
        if (upper) {
            mPopup.update(mInitialX + (mPrimaryWidth - mSecondaryWidth),
                    mInitialY - mSecondaryHeight, mSecondaryWidth, mPrimaryHeight + mSecondaryHeight);
        } else {
            mPopup.update(mInitialX + (mPrimaryWidth - mSecondaryWidth),
                    mInitialY, mSecondaryWidth, mPrimaryHeight + mSecondaryHeight);
        }
    }

    private void onHideMoreOptions() {
        mPrimaryOptionsViewBlock.setVisibility(View.VISIBLE);
        mSecondaryOptionsViewUpperBlock.setVisibility(View.GONE);
        mSecondaryOptionsViewLowerBlock.setVisibility(View.GONE);
        mPopup.update(mInitialX, mInitialY, mPrimaryWidth, mPrimaryHeight);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean hasOption(int option) {
        if (mReadOnly & (option == OPTION_CUT || option == OPTION_PASTE)) {
            return false;
        }
        if (!mHasSelection & (option == OPTION_CUT
                || option == OPTION_COPY || option == OPTION_SEARCH
                || option >= basicOptionsLength)) {
            return false;
        }
        if (option == OPTION_PASTE) {
            return mClipboard.hasPrimaryClip()
                    && mClipboard.getPrimaryClip() != null
                    && mClipboard.getPrimaryClipDescription() != null
                    && mClipboard.getPrimaryClipDescription().hasMimeType("text/*")
                    && !TextUtils.isEmpty(mClipboard.getPrimaryClip().getItemAt(0).getText());
        }
        return true;
    }

    private int[] measureView(View v) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(widthMeasureSpec, heightMeasureSpec);
        return new int[]{v.getMeasuredWidth(), v.getMeasuredHeight()};
    }

    private boolean isUpperBlock() {
        return mInitialY > (mMetrics.heightPixels / 2);
    }
}
