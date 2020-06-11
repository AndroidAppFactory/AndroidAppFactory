package com.bihe0832.android.lib.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;


public abstract class BaseTableModel {

    protected static int deleteAll(BaseDBHelper helper, String tableName) {
        int rows = 0;

        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            rows = db.delete(tableName, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            rows = 0;
        } finally {
        }

        return rows;
    }

    protected static void putValues(ContentValues cv, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            cv.put(key, value);
        } else {
            cv.put(key, "");
        }
    }

    protected static void putValues(ContentValues cv, String key, int value) {
        cv.put(key, value);
    }

    protected static void putValues(ContentValues cv, String key, long value) {
        cv.put(key, value);
    }


    protected static String getStringByName(Cursor c, String columnName) {
        return c.getString(c.getColumnIndex(columnName));
    }

    protected static int getIntByName(Cursor c, String columnName) {
        return c.getInt(c.getColumnIndex(columnName));
    }

    protected static long getLongByName(Cursor c, String columnName) {
        return c.getLong(c.getColumnIndex(columnName));
    }
}
