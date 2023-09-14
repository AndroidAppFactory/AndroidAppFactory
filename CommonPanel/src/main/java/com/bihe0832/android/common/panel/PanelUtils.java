package com.bihe0832.android.common.panel;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import com.bihe0832.android.common.panel.data.PanelStorageManager;
import com.bihe0832.android.lib.panel.PanelManager;
import com.bihe0832.android.lib.panel.bean.BoardsInfo;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/9/14.
 *         Description:
 */
public class PanelUtils {

    public static final void loadPanelByPath(Context context, String path) {
        PanelManager.getInstance().mFilePath = path;
        BoardsInfo points = PanelStorageManager.loadBoardByPath(path);
        startPanel(context, points);
    }

    public static final void loadPanelByContent(Context context, String panelContent) {
        BoardsInfo points = PanelStorageManager.loadBoardByContent(panelContent);
        startPanel(context, points);
    }

    public static final void startPanel(Context context, String path, int orientation) {
        PanelManager.getInstance().mFilePath = path;
        startPanel(context, orientation);
    }

    public static final void startPanel(Context context, BoardsInfo points) {
        if (null != points) {
            PanelManager.getInstance().initDrawBoard(points);
            startPanel(context, PanelManager.getInstance().getDrawBoard().getOrientation());
        } else {
            startPanel(context, Configuration.ORIENTATION_LANDSCAPE);
        }
    }

    public static final void startPanel(Context context, int orientation) {
        if (PanelManager.getInstance().getDrawBoard() == null) {
            PanelManager.getInstance().initDrawBoard(orientation);
        }
        Intent intent;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            intent = new Intent(context, PanelPortraitActivity.class);
        } else {
            intent = new Intent(context, PanelLandscapeActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
