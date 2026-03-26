package com.bihe0832.android.common.message.data.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bihe0832.android.lib.sqlite.BaseDBHelper;


class MessageDBHelper extends BaseDBHelper {
    private static final String DB_NAME = "zixie_message";

    private static final int DB_VERSION = 2;

    MessageDBHelper(Context ctx) {
        super(ctx, DB_NAME, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(MessageTableModel.TABLE_CREATE_SQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(MessageTableModel.TABLE_DROP_SQL);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Override")
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(MessageTableModel.TABLE_DROP_SQL);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
