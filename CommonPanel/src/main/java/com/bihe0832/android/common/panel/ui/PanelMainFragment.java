package com.bihe0832.android.common.panel.ui;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bihe0832.android.common.panel.R;
import com.bihe0832.android.common.panel.data.PanelStorageManager;
import com.bihe0832.android.common.panel.ui.menu.DragLinearLayout;
import com.bihe0832.android.common.panel.ui.menu.SizeDialogUtils;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.color.picker.dialog.ColorDialogUtils;
import com.bihe0832.android.lib.color.utils.ColorUtils;
import com.bihe0832.android.lib.media.image.CheckedEnableImageView;
import com.bihe0832.android.lib.panel.PanelManager;
import com.bihe0832.android.lib.panel.bean.DrawPoint;
import com.bihe0832.android.lib.panel.bean.DrawTextPoint;
import com.bihe0832.android.lib.panel.constants.DrawEvent;
import com.bihe0832.android.lib.panel.event.DrawEventLiveData;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils;
import com.bihe0832.android.lib.ui.view.ext.DrawableFactoryKt;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/9/6.
 *         Description:
 */
public class PanelMainFragment extends BaseFragment {

    private String lastSaveInfo = "";

    private ImageView draw_type_line = null;
    private ImageView draw_type_text = null;
    private ImageView draw_type_earease = null;

    private ImageView line_settings = null;
    private ImageView color_settings = null;

    private ImageView text_size_settings = null;
    private CheckedEnableImageView text_bold_settings = null;
    private CheckedEnableImageView text_underline_settings = null;
    private CheckedEnableImageView text_italics_settings = null;
    private ImageView draw_action_undo = null;
    private ImageView draw_action_redo = null;
    private CheckedEnableImageView draw_action_status = null;
    private ImageView draw_action_add_page = null;
    private ImageView draw_action_page_pre = null;
    private TextView draw_action_page_text = null;
    private ImageView draw_action_page_next = null;
    private ImageView draw_action_export = null;
    private ImageView draw_action_save = null;
    private ImageView draw_action_close = null;


    private DragLinearLayout menu_switch_layout = null;
    private ImageView menu_switch = null;
    private LinearLayout menu_layout = null;
    private LinearLayout toolbar_layout = null;

    @Override
    protected int getLayoutID() {
        return R.layout.com_bihe0832_common_fragment_panel_main;
    }

    @Override
    protected void initView(@NonNull View view) {
        super.initView(view);
        loadRootFragment(R.id.draw_layout, new PanelDrawFragment());
        initMenu(view);
        initPenView(view);
        initTextView(view);
        initEarease(view);
        initColor(view);
        initLineSettings(view);
        initTextStyleSettings(view);
        initRedoUndo(view);
        initDrawStatus(view);
        initPageStatus(view);
        initExportAndSave(view);
        DrawEventLiveData.INSTANCE.observe(this, event -> {
            switch (event) {
                case DrawEvent.STATUS_OUTSIDE_SELECTED:
                    updateMenu();
                case DrawEvent.STATUS_DRAW_START:
                case DrawEvent.STATUS_REDO_UNDO_CHANGED:
                    updateRedoUndo();
                    break;
                case DrawEvent.STATUS_TYPE_CHANGED:
                    updateTypeStatus();
                    updateMenu();
                    break;
                case DrawEvent.STATUS_TEXT_SELECTED:
                    if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_TEXT) {
                        showTextSetting();
                    }
                case DrawEvent.STATUS_PAGE_CHANGED:
                    lastSaveInfo = "";
                    updatePages();
                    break;
            }
        });
        updateMenu();
        updateTypeStatus();
        updatePages();
        updateRedoUndo();
    }

    private int getColorAccent() {
        return ThemeResourcesManager.INSTANCE.getColor(com.bihe0832.android.lib.aaf.res.R.color.colorAccent);
    }

    private void initMenu(View view) {
        menu_switch_layout = view.findViewById(R.id.menu_switch_layout);
        menu_switch_layout.setBackground(
                DrawableFactoryKt.generateCornerRadiusDrawable(menu_switch_layout, getColorAccent(),
                        DisplayUtil.dip2px(getContext(), 8f)));
        menu_switch_layout.setClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (menu_layout.getVisibility() == View.VISIBLE) {
                    menu_layout.setVisibility(View.GONE);
                    if (null != toolbar_layout) {
                        toolbar_layout.setVisibility(View.GONE);
                    }
                } else {
                    menu_layout.setVisibility(View.VISIBLE);
                    if (null != toolbar_layout) {
                        toolbar_layout.setVisibility(View.VISIBLE);
                    }
                }
                ThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_RESET);
                    }
                }, 100L);

            }
        });
        menu_switch = view.findViewById(R.id.menu_switch);
        menu_layout = view.findViewById(R.id.menu_layout);
        try {
            toolbar_layout = view.findViewById(R.id.toolbar_layout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPenView(View view) {
        draw_type_line = view.findViewById(R.id.draw_line);
        draw_type_line.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.TYPE_DRAW_PEN);
            }
        });
    }


    private void initTextView(View view) {
        draw_type_text = view.findViewById(R.id.draw_text);
        draw_type_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.TYPE_DRAW_TEXT);
            }
        });
    }


    private void initEarease(View view) {
        draw_type_earease = view.findViewById(R.id.draw_earease);
        draw_type_earease.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.TYPE_DRAW_ERASER);
                hidePenSetting();
            }
        });
    }

    private void initColor(View view) {
        color_settings = view.findViewById(R.id.color_settings);
        color_settings.setColorFilter(PanelManager.getInstance().mCurrentColor);
        color_settings.setOnClickListener(v -> {
            int color = PanelManager.getInstance().mCurrentColor;
            color_settings.setColorFilter(null);
            ColorDialogUtils.showColorSelectDialog(getContext(), ColorUtils.getAlpha(color),
                    ColorUtils.removeAlpha(color),
                    newColor -> {
                        PanelManager.getInstance().mCurrentColor = newColor;
                        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_COLOR_CHANGED);
                        color_settings.setColorFilter(PanelManager.getInstance().mCurrentColor);
                    });
        });
    }

    private void initLineSettings(View view) {
        line_settings = view.findViewById(R.id.line_settings);
        line_settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = PanelManager.getInstance().mCurrentPenSize;
                if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_ERASER) {
                    current = PanelManager.getInstance().mCurrentEraserSize;
                }
                SizeDialogUtils.showLineSizeSelectDialog(getContext(), current, 100, size -> {
                    if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_ERASER) {
                        PanelManager.getInstance().mCurrentEraserSize = size;
                        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_ERASER_CHANGED);
                    } else {
                        PanelManager.getInstance().mCurrentPenSize = size;
                        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_PEN_CHANGED);
                    }
                });
            }
        });
    }

    private void initTextStyleSettings(View view) {
        text_size_settings = view.findViewById(R.id.text_size_settings);
        text_size_settings.setOnClickListener(
                v -> SizeDialogUtils.showTextSizeSelectDialog(
                        getContext(), PanelManager.getInstance().mCurrentTextSize, 100,
                        size -> {
                            PanelManager.getInstance().mCurrentTextSize = size;
                            DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_TEXT_STYLE_CHANGED);
                        }));

        text_bold_settings = view.findViewById(R.id.text_bold_settings);
        text_bold_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawPoint dp = PanelManager.getInstance().getCurrentDrawPoint();
                if (dp.getType() == DrawEvent.TYPE_DRAW_TEXT) {
                    dp.getDrawText().setIsBold(!dp.getDrawText().getIsBold());
                }
                text_bold_settings.setChecked(!text_bold_settings.isChecked());
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_TEXT_STYLE_CHANGED);
            }
        });

        text_italics_settings = view.findViewById(R.id.text_italics_settings);
        text_italics_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawPoint dp = PanelManager.getInstance().getCurrentDrawPoint();
                if (dp.getType() == DrawEvent.TYPE_DRAW_TEXT) {
                    dp.getDrawText().setIsItalics(!dp.getDrawText().getIsItalics());
                }
                text_italics_settings.setChecked(!text_italics_settings.isChecked());
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_TEXT_STYLE_CHANGED);
            }
        });

        text_underline_settings = view.findViewById(R.id.text_underline_settings);
        text_underline_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawPoint dp = PanelManager.getInstance().getCurrentDrawPoint();
                if (dp.getType() == DrawEvent.TYPE_DRAW_TEXT) {
                    dp.getDrawText().setIsUnderline(!dp.getDrawText().getIsUnderline());
                }
                text_underline_settings.setChecked(!text_underline_settings.isChecked());
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_TEXT_STYLE_CHANGED);
            }
        });
    }

    private void initRedoUndo(View view) {
        draw_action_redo = view.findViewById(R.id.draw_redo);
        draw_action_redo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.ACTION_REDO);
            }
        });

        draw_action_undo = view.findViewById(R.id.draw_undo);
        draw_action_undo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.ACTION_UNDO);
            }
        });
    }

    private void initDrawStatus(View view) {
        draw_action_status = view.findViewById(R.id.allStatus);
        draw_action_status.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PanelManager.getInstance().ENABLE = !PanelManager.getInstance().ENABLE;
                draw_action_status.setChecked(PanelManager.getInstance().ENABLE);
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_OUTSIDE_SELECTED);
            }
        });
    }

    private void initPageStatus(View view) {
        draw_action_add_page = view.findViewById(R.id.add_page);
        draw_action_add_page.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.ACTION_PAGE_NEW);
            }
        });
        draw_action_page_pre = view.findViewById(R.id.page_pre);
        draw_action_page_pre.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.ACTION_PAGE_PRE);
            }
        });
        draw_action_page_next = view.findViewById(R.id.page_next);
        draw_action_page_next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.ACTION_PAGE_NEXT);
            }
        });
        draw_action_page_text = view.findViewById(R.id.tv_white_board_page);
    }

    private void initExportAndSave(View view) {
        lastSaveInfo = getSaveInfo();

        draw_action_export = view.findViewById(R.id.export);
        draw_action_export.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.ACTION_EXPORT);
            }
        });

        draw_action_save = view.findViewById(R.id.save);
        draw_action_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                lastSaveInfo = getSaveInfo();
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.ACTION_SAVE);
                savePage();
                DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_SAVED);
            }
        });

        draw_action_close = view.findViewById(R.id.close);
        draw_action_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedSupport();
            }
        });
    }

    private void savePage() {
        if (TextUtils.isEmpty(PanelManager.getInstance().mFilePath)) {
            PanelManager.getInstance().mFilePath = PanelStorageManager.getPanelSavePath();
        }
        PanelStorageManager.saveBoard(PanelManager.getInstance().mFilePath);
    }

    private void updateTypeStatus() {
        int currentType = PanelManager.getInstance().mCurrentDrawType;
        if (DrawEvent.TYPE_DRAW_PEN == currentType) {
            hideTextSetting();
            hideEreaseSetting();
            showPenSetting();
        } else if (DrawEvent.TYPE_DRAW_TEXT == currentType) {
            hidePenSetting();
            hideEreaseSetting();
            showTextSetting();
        } else if (DrawEvent.TYPE_DRAW_ERASER == currentType) {
            hidePenSetting();
            hideTextSetting();
            showEreaseSetting();
        }
    }


    private void showPenSetting() {
        draw_type_line.setColorFilter(getColorAccent());
        line_settings.setVisibility(View.VISIBLE);
        color_settings.setVisibility(View.VISIBLE);
    }


    private void hidePenSetting() {
        draw_type_line.setColorFilter(null);
        line_settings.setVisibility(View.GONE);
        color_settings.setVisibility(View.GONE);
    }

    private void showEreaseSetting() {
        draw_type_earease.setColorFilter(getColorAccent());
        line_settings.setVisibility(View.VISIBLE);
    }


    private void hideEreaseSetting() {
        draw_type_earease.setColorFilter(null);
        line_settings.setVisibility(View.GONE);
    }

    private void showTextSetting() {
        draw_type_text.setColorFilter(getColorAccent());
        color_settings.setVisibility(View.VISIBLE);
        text_size_settings.setVisibility(View.VISIBLE);
        text_bold_settings.setVisibility(View.VISIBLE);
        DrawTextPoint point = PanelManager.getInstance().getCurrentDrawPoint().getDrawText();
        text_bold_settings.setChecked(point == null ? false : point.getIsBold());
        text_underline_settings.setVisibility(View.VISIBLE);
        text_underline_settings.setChecked(point == null ? false : point.getIsUnderline());
        text_italics_settings.setVisibility(View.GONE);

    }


    private void hideTextSetting() {
        draw_type_text.setColorFilter(null);
        color_settings.setVisibility(View.GONE);
        text_size_settings.setVisibility(View.GONE);
        text_bold_settings.setVisibility(View.GONE);
        text_underline_settings.setVisibility(View.GONE);
        text_italics_settings.setVisibility(View.GONE);
    }

    private void updatePages() {
        int current = PanelManager.getInstance().mCurrentIndex + 1;
        int total = PanelManager.getInstance().getCurrentBoardPointSize();
        draw_action_page_text.setText(current + "/" + total);
        if (current > 1) {
            draw_action_page_pre.setVisibility(View.VISIBLE);
        } else {
            draw_action_page_pre.setVisibility(View.INVISIBLE);
        }
        if (total > 1 && current != total) {
            draw_action_page_next.setVisibility(View.VISIBLE);
        } else {
            draw_action_page_next.setVisibility(View.INVISIBLE);
        }
    }

    private void updateRedoUndo() {
        if (PanelManager.getInstance().getCurrentPagePoints().isEmpty()) {
            draw_action_undo.setVisibility(View.INVISIBLE);
        } else {
            draw_action_undo.setVisibility(View.VISIBLE);
        }
        if (PanelManager.getInstance().getCurrentPageDeletePoints().isEmpty()) {
            draw_action_redo.setVisibility(View.INVISIBLE);
        } else {
            draw_action_redo.setVisibility(View.VISIBLE);
        }

        draw_action_status.setChecked(PanelManager.getInstance().ENABLE);
    }

    private void updateMenu() {
        if (PanelManager.getInstance().ENABLE == false) {
            menu_switch.setImageResource(R.drawable.com_bihe0832_common_panel_status_view);
        } else if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_PEN) {
            menu_switch.setImageResource(R.drawable.com_bihe0832_common_panel_action_pen);
        } else if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_TEXT) {
            menu_switch.setImageResource(R.drawable.com_bihe0832_common_panel_action_text);
        } else if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_ERASER) {
            menu_switch.setImageResource(R.drawable.com_bihe0832_common_panel_action_earease);
        }
    }

    private String getSaveInfo() {
        int current = PanelManager.getInstance().mCurrentIndex;
        int total = PanelManager.getInstance().getCurrentBoardPointSize();
        int currentPage = PanelManager.getInstance().getCurrentPagePoints().size();
        int currentDelete = PanelManager.getInstance().getCurrentPageDeletePoints().size();
        if (current + total + currentPage + currentDelete == 1) {
            return "";
        } else {
            return current + "/" + total + "/" + currentPage + "/" + currentDelete;
        }
    }

    private void onBack() {
        getActivity().finish();
        PanelManager.getInstance().distory();
    }

    @Override
    public boolean onBackPressedSupport() {
        String newInfo = getSaveInfo();
        if (lastSaveInfo.equals(newInfo)) {
            onBack();
        } else {
            DialogUtils.INSTANCE.showConfirmDialog(
                    getContext(),
                    ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.model.res.R.string.dialog_title),
                    "当前画作尚未保存，确认要退出么？",
                    "保存并退出",
                    "直接退出",
                    true,
                    new OnDialogListener() {
                        @Override
                        public void onPositiveClick() {
                            savePage();
                            ThreadManager.getInstance().start(new Runnable() {
                                @Override
                                public void run() {
                                    onBack();
                                }
                            }, 200L);
                        }

                        @Override
                        public void onNegativeClick() {
                            ThreadManager.getInstance().start(new Runnable() {
                                @Override
                                public void run() {
                                    onBack();
                                }
                            }, 100L);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        }
        return true;
    }
}
