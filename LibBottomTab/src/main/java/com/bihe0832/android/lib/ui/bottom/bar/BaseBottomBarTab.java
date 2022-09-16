package com.bihe0832.android.lib.ui.bottom.bar;

import android.view.View;

public interface BaseBottomBarTab {

    void setSelected(boolean selected);

    void setTabPosition(int position);

    void setUnreadCount(int num);

    void setUnreadDot(boolean visible);

    int getTabPosition();

    View getTabView();
}
