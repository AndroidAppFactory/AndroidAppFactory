package com.bihe0832.android.lib.sqlite.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.bihe0832.android.lib.sqlite.BaseDBHelper;
import com.bihe0832.android.lib.sqlite.BaseTableModel;

class CommonTableModel extends BaseTableModel {
    public static final String TAG = "CommonTableModel";
    static final String TABLE_NAME = "common_info";

    private static final String col_id = "id";
    private static final String col_key = "key";
    private static final String col_value = "value";
    private static final String col_create_at = "create_at";
    private static final String col_update_at = "update_at";

    static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS ["
            + TABLE_NAME
            + "] ("
            + "[" + col_id + "] INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "[" + col_key + "] NVARCHAR(128)  NULL,"
            + "[" + col_value + "] VARCHAR(256)  NULL,"
            + "[" + col_create_at + "] TIMESTAMP  NULL,"
            + "[" + col_update_at + "] TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL"
            + ")";
    static final String TABLE_DROP_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;

    static int deleteAll(BaseDBHelper helper) {
        return deleteAll(helper, TABLE_NAME);
    }

    private static ContentValues data2CV(String key, String value) {
        ContentValues cv = new ContentValues();
        putValues(cv, col_key, key);
        if (!TextUtils.isEmpty(value)) {
            putValues(cv, col_value, value);
        }
        putValues(cv, col_update_at, System.currentTimeMillis());
        return cv;
    }

    private static String getColumnData(Cursor cursor) {
        String result = "";
        try {
            if (null != cursor && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = getStringByName(cursor, col_value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    static String getData(BaseDBHelper helper, String key) {
        String[] columns = null;
        String selection = " " + col_key + " = ? ";
        String[] selectionArgs = {key};
        String groupBy = null;
        String having = null;
        String orderBy = " `" + col_update_at + "` DESC ";
        String limit = " 1 ";
        Cursor cursor = helper.queryInfo(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

        String data = getColumnData(cursor);

        if (cursor != null) {
            cursor.close();
        }
        return data;
    }

    private static boolean insertData(BaseDBHelper helper, String key, String value) {
        ContentValues values = data2CV(key, value);
        putValues(values, col_create_at, System.currentTimeMillis());
        long id = helper.insert(TABLE_NAME, null, values);
        return (id != -1);
    }

    private static boolean updateData(BaseDBHelper helper, String key, String value) {
        ContentValues values = data2CV(key, value);
        String whereClause = " `" + col_key + "` = ? ";
        String[] whereArgs = new String[]{key};
        int rows = helper.update(TABLE_NAME, values, whereClause, whereArgs);
        return (rows != 0);
    }

    private static boolean hasData(BaseDBHelper helper, String key) {
        String[] columns = null;
        String selection = " " + col_key + " = ? ";
        String[] selectionArgs = {key};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        boolean find = false;
        Cursor cursor = helper.queryInfo(TABLE_NAME, columns,
                selection, selectionArgs, groupBy, having, orderBy, limit);
        try {
            find = (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            cursor.close();
        }
        return find;
    }

    static boolean saveData(BaseDBHelper helper, String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        boolean success;
        if (hasData(helper, key)) {
            success = updateData(helper, key, value);
        } else {
            success = insertData(helper, key, value);
        }

        return success;
    }

    static boolean clearData(BaseDBHelper helper,String key) {
        return  updateData(helper,key,"");
    }
}
