package com.bihe0832.android.common.message.data.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.bihe0832.android.common.message.data.MessageInfoItem;
import com.bihe0832.android.lib.gson.JsonHelper;
import com.bihe0832.android.lib.sqlite.BaseDBHelper;
import com.bihe0832.android.lib.sqlite.BaseTableModel;
import com.bihe0832.android.lib.utils.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

class MessageTableModel extends BaseTableModel {
    static final String TABLE_NAME = "message_info";

    private static final String col_id = "id";

    private static final String col_msg_id = "msg_id";
    private static final String col_data = "msg_data";
    private static final String col_read = "msg_read";
    private static final String col_delete = "msg_delete";
    private static final String col_last_show = "msg_last_show";

    private static final String col_create_at = "create_at";
    private static final String col_update_at = "update_at";

    static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "] (" + "[" + col_id + "] INTEGER PRIMARY KEY AUTOINCREMENT," + "[" + col_msg_id + "] NVARCHAR(128)  NULL," + "[" + col_data + "] VARCHAR(256)  NULL," + "[" + col_delete + "] VARCHAR(8)  NULL," + "[" + col_read + "] VARCHAR(8)  NULL," + "[" + col_last_show + "] TIMESTAMP  NULL," + "[" + col_create_at + "] TIMESTAMP  NULL," + "[" + col_update_at + "] TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL" + ")";
    static final String TABLE_DROP_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;

    static int deleteAll(BaseDBHelper helper) {
        return deleteAll(helper, TABLE_NAME);
    }

    private static ContentValues data2CV(MessageInfoItem info) {
        ContentValues cv = new ContentValues();
        if (null != info) {
            putValues(cv, col_msg_id, info.getMessageID());
            putValues(cv, col_data, JsonHelper.INSTANCE.toJson(info));
            putValues(cv, col_last_show, String.valueOf(info.getLastShow()));
            putValues(cv, col_delete, String.valueOf(info.hasDelete()));
            putValues(cv, col_read, String.valueOf(info.hasRead()));
        }
        putValues(cv, col_update_at, System.currentTimeMillis());
        return cv;
    }

    private static MessageInfoItem getColumnData(Cursor cursor) {
        try {
            if (null != cursor && cursor.getCount() > 0) {
                cursor.moveToFirst();
                MessageInfoItem item = JsonHelper.INSTANCE.fromJson(getStringByName(cursor, col_data), MessageInfoItem.class);
                item.setLastShow(ConvertUtils.parseLong(getStringByName(cursor, col_last_show), 0L));
                item.setHasDelete(ConvertUtils.parseBoolean(getStringByName(cursor, col_delete), false));
                item.setHasRead(ConvertUtils.parseBoolean(getStringByName(cursor, col_read), false));
                return item;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static List<MessageInfoItem> getAllData(BaseDBHelper helper) {
        ArrayList<MessageInfoItem> dataList = new ArrayList<>();
        String[] columns = null;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = " `" + col_update_at + "` DESC ";
        String limit = null;
        Cursor cursor = helper.queryInfo(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        try {
            while (cursor.moveToNext()) {
                MessageInfoItem info = JsonHelper.INSTANCE.fromJson(getStringByName(cursor, col_data), MessageInfoItem.class);
                info.setLastShow(ConvertUtils.parseLong(getStringByName(cursor, col_last_show), 0L));
                info.setHasDelete(ConvertUtils.parseBoolean(getStringByName(cursor, col_delete), false));
                info.setHasRead(ConvertUtils.parseBoolean(getStringByName(cursor, col_read), false));
                if (info != null) {
                    dataList.add(info);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

    static MessageInfoItem getData(BaseDBHelper helper, String msgID) {
        String[] columns = null;
        String selection = " " + col_msg_id + " = ? ";
        String[] selectionArgs = {msgID};
        String groupBy = null;
        String having = null;
        String orderBy = " `" + col_update_at + "` DESC ";
        String limit = " 1 ";
        Cursor cursor = helper.queryInfo(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

        MessageInfoItem data = getColumnData(cursor);

        if (cursor != null) {
            cursor.close();
        }
        return data;
    }

    private static boolean insertData(BaseDBHelper helper, MessageInfoItem info) {
        ContentValues values = data2CV(info);
        putValues(values, col_create_at, System.currentTimeMillis());
        long id = helper.insert(TABLE_NAME, null, values);
        return (id != -1);
    }

    private static boolean updateData(BaseDBHelper helper, MessageInfoItem info) {
        ContentValues values = data2CV(info);
        String whereClause = " `" + col_msg_id + "` = ? ";
        String[] whereArgs = new String[]{info.getMessageID()};
        int rows = helper.update(TABLE_NAME, values, whereClause, whereArgs);
        return (rows != 0);
    }

    private static boolean hasData(BaseDBHelper helper, MessageInfoItem info) {
        String[] columns = null;
        String selection = " " + col_msg_id + " = ? ";
        String[] selectionArgs = {info.getMessageID()};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        boolean find = false;
        Cursor cursor = helper.queryInfo(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
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

    static boolean saveData(BaseDBHelper helper, MessageInfoItem info) {
        boolean success;
        if (hasData(helper, info)) {
            success = updateData(helper, info);
        } else {
            success = insertData(helper, info);
        }

        return success;
    }

    static boolean clearData(BaseDBHelper helper, MessageInfoItem info) {
        if (info != null) {
            info.setHasDelete(true);
            return updateData(helper, info);
        } else {
            return true;
        }
    }
}
