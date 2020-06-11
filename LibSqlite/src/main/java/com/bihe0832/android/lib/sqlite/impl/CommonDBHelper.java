package com.bihe0832.android.lib.sqlite.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bihe0832.android.lib.sqlite.BaseDBHelper;


class CommonDBHelper extends BaseDBHelper {
    private static final String DB_NAME = "zixie";

    private static final int DB_VERSION = 1;

    CommonDBHelper(Context ctx) {
        super(ctx, DB_NAME, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CommonTableModel.TABLE_CREATE_SQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(CommonTableModel.TABLE_DROP_SQL);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Override")
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(CommonTableModel.TABLE_DROP_SQL);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
