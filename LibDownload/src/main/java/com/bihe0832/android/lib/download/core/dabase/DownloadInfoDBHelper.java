package com.bihe0832.android.lib.download.core.dabase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.sqlite.BaseDBHelper;

/**
 * 下载进度保存
 *
 * @author zixie code@bihe0832.com Created on 2020/6/12.
 */
class DownloadInfoDBHelper extends BaseDBHelper {

    private static final String DB_NAME = "zixie_download";

    private static final int DB_VERSION = 13;

    DownloadInfoDBHelper(Context ctx) {
        super(ctx, DB_NAME, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            ZLog.d(DownloadPartInfoTableModel.TABLE_CREATE_SQL);
            db.execSQL(DownloadPartInfoTableModel.TABLE_CREATE_SQL);
            ZLog.d(DownloadInfoTableModel.TABLE_CREATE_SQL);
            db.execSQL(DownloadInfoTableModel.TABLE_CREATE_SQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(DownloadPartInfoTableModel.TABLE_DROP_SQL);
            db.execSQL(DownloadInfoTableModel.TABLE_DROP_SQL);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Override")
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(DownloadPartInfoTableModel.TABLE_DROP_SQL);
            db.execSQL(DownloadInfoTableModel.TABLE_DROP_SQL);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}