package com.bihe0832.android.lib.panel.constants;


public class DrawEvent {

    public static final int TYPE_START = 1;
    /**
     * 画笔模式
     */
    public static final int TYPE_DRAW_PEN = TYPE_START + 1;
    /**
     * 文字模式
     */
    public static final int TYPE_DRAW_TEXT = TYPE_DRAW_PEN + 1;
    /**
     * 橡皮擦模式
     */
    public static final int TYPE_DRAW_ERASER = TYPE_DRAW_TEXT + 1;


    public static final int STATUS_START = 20;
    public static final int STATUS_SAVED = STATUS_START + 1;

    public static final int STATUS_RESET = STATUS_SAVED + 1;

    public static final int STATUS_DRAW_START = STATUS_RESET + 1;
    /**
     * 点击外围
     */
    public static final int STATUS_OUTSIDE_SELECTED = STATUS_DRAW_START + 1;
    public static final int STATUS_TEXT_SELECTED = STATUS_OUTSIDE_SELECTED + 1;

    /**
     * 画笔属性调整
     */
    public static final int STATUS_PEN_CHANGED = 25;

    /**
     * 颜色属性调整
     */
    public static final int STATUS_COLOR_CHANGED = STATUS_PEN_CHANGED + 1;

    /**
     * 文字属性调整
     */
    public static final int STATUS_TEXT_STYLE_CHANGED = STATUS_COLOR_CHANGED + 1;

    public static final int STATUS_TYPE_CHANGED = STATUS_TEXT_STYLE_CHANGED + 1;
    /**
     * 橡皮擦属性调整
     */
    public static final int STATUS_ERASER_CHANGED = STATUS_TYPE_CHANGED + 1;
    public static final int STATUS_PAGE_CHANGED = STATUS_ERASER_CHANGED + 1;
    public static final int STATUS_REDO_UNDO_CHANGED = STATUS_PAGE_CHANGED + 1;

    public static final int ACTION_START = 60;

    public static final int ACTION_REDO = ACTION_START + 1;
    public static final int ACTION_UNDO = ACTION_REDO + 1;
    public static final int ACTION_SAVE = ACTION_UNDO + 1;
    public static final int ACTION_EXPORT = 70;
    public static final int ACTION_PAGE_NEW = ACTION_EXPORT + 1;

    public static final int ACTION_PAGE_NEXT = ACTION_PAGE_NEW + 1;

    public static final int ACTION_PAGE_PRE = ACTION_PAGE_NEXT + 1;
}
