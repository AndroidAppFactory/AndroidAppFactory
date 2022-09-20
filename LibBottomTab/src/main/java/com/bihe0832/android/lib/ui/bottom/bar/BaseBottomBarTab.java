package com.bihe0832.android.lib.ui.bottom.bar;

import android.view.View;

public interface BaseBottomBarTab {

    void setSelected(boolean selected);

    void setTabPosition(int position);

    void showUnreadMsg(int num);

    int getTabPosition();

    View getTabView();
}
