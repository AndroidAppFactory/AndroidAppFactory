package com.bihe0832.android.lib.download.dabase;

import android.content.ContentValues;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;

import com.bihe0832.android.lib.download.DownloadItem;
import com.bihe0832.android.lib.download.DownloadStatus;
import com.bihe0832.android.lib.download.core.DownloadManager;
import com.bihe0832.android.lib.download.core.list.DownloadTaskList;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.sqlite.BaseDBHelper;
import com.bihe0832.android.lib.sqlite.BaseTableModel;
import com.bihe0832.android.lib.utils.apk.APKUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import kotlin.jvm.Synchronized;

/**
 * DownloadInfoTableModel
 *
 * @author zixie code@bihe0832.com Created on 2020/6/12.
 */
public class DownloadInfoTableModel extends BaseTableModel {

    static final String TABLE_NAME = "download_info";

    public static final String col_id = "id";
    public static final String col_download_id = "download_id";
    public static final String col_download_package = "download_package";
    public static final String col_download_content = "download_content";

    static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS ["
            + TABLE_NAME
            + "] ("
            + "[" + col_id + "] INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "[" + col_download_id + "] NVARCHAR(128)  NULL,"
            + "[" + col_download_package + "] NVARCHAR(128)  NULL,"
            + "[" + col_download_content + "] VARCHAR(102400)  NULL"
            + ")";
    static final String TABLE_DROP_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;

    static int deleteAll(BaseDBHelper helper) {
        return deleteAll(helper, TABLE_NAME);
    }

    private static ContentValues data2CV(DownloadItem item) {
        ContentValues cv = new ContentValues();

        try {
            putValues(cv, col_download_id, item.getDownloadID());
            putValues(cv, col_download_package, item.getPackageName());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(item);
            String res = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            oos.close();
            putValues(cv, col_download_content, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cv;
    }

    private static DownloadItem cv2data(Cursor cursor) {
        try {
            byte[] data = Base64.decode(getStringByName(cursor, col_download_content), Base64.DEFAULT);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            DownloadItem item = (DownloadItem) ois.readObject();
            ois.close();
            int status = item.getStatus();
            if (status == DownloadStatus.STATUS_DOWNLOADING || status == DownloadStatus.STATUS_DOWNLOAD_STARTED
                    || status == DownloadStatus.STATUS_DOWNLOAD_WAITING) {
                item.setStatus(DownloadStatus.STATUS_DOWNLOAD_PAUSED);
            } else if (status == DownloadStatus.STATUS_HAS_DOWNLOAD
                    || status == DownloadStatus.STATUS_DOWNLOAD_SUCCEED) {
                PackageInfo info = APKUtils
                        .getInstalledPackage(DownloadManager.INSTANCE.getContext(), item.getPackageName());
                if (info != null && info.versionCode == item.getVersionCode()) {
                    item.setStatus(status);
                } else {
                    item.setStatus(DownloadStatus.NO_DOWNLOAD);
                }
            } else {
                item.setStatus(status);
            }
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DownloadItem();
    }

    @Synchronized
    static void initData(BaseDBHelper helper) {
        try {
            Cursor cursor = helper.queryInfo(TABLE_NAME, null, null, null, null, null, null, null);
            try {
                DownloadTaskList.INSTANCE.clear();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    DownloadItem item = cv2data(cursor);
                    item.setFinishedLengthBefore(
                            DownloadInfoDBManager.INSTANCE.getFinishedBefore(item.getDownloadID()));
                    item.setFinished(item.getFinishedLengthBefore());
                    DownloadTaskList.INSTANCE.addToDownloadTaskList(item);
                    if (FileUtils.INSTANCE.checkFileExist(item.getFilePath())) {
                        item.setFinished(item.getFileLength());
                        item.setStatus(DownloadStatus.STATUS_DOWNLOAD_SUCCEED);
                    }
                    long id = getLongByName(cursor, col_id);
                    ZLog.d("数据库信息：id:" + id + "info :" + item.toString());
                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean insertData(BaseDBHelper helper, DownloadItem item) {
        ContentValues values = data2CV(item);
        long id = helper.insert(TABLE_NAME, null, values);
        return (id != -1);
    }

    private static boolean updateData(BaseDBHelper helper, DownloadItem item) {
        ContentValues values = data2CV(item);
        String whereClause = " `" + col_download_id + "` = ? ";
        String[] whereArgs = new String[]{String.valueOf(item.getDownloadID())};
        int rows = helper.update(TABLE_NAME, values, whereClause, whereArgs);
        return (rows != 0);
    }

    static boolean hasData(BaseDBHelper helper, String url) {
        String[] columns = null;
        String selection = " " + col_download_id + " = ? ";
        String[] selectionArgs = {String.valueOf(DownloadItem.getDownloadIDByURL(url))};
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
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return find;
    }

    static DownloadItem getDownloadInfoFromPackageName(BaseDBHelper helper, String packageName) {
        String selection = " " + col_download_package + " = ? ";
        String[] selectionArgs = {packageName};
        return getDownloadInfoFromDBBySection(helper, selection, selectionArgs);
    }

    static DownloadItem getDownloadInfoFromDBBySection(BaseDBHelper helper, String selection, String[] selectionArgs) {
        String[] columns = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        Cursor cursor = helper
                .queryInfo(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        DownloadItem dataItem = null;
        try {
            if (cursor.getCount() < 1) {
                dataItem = null;
            } else {
                cursor.moveToFirst();
                dataItem = cv2data(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return dataItem;
    }

    static DownloadItem getDownloadInfo(BaseDBHelper helper, String url) {
        String selection = " " + col_download_id + " = ? ";
        String[] selectionArgs = {String.valueOf(DownloadItem.getDownloadIDByURL(url))};
        return getDownloadInfoFromDBBySection(helper, selection, selectionArgs);

    }

    static boolean saveData(BaseDBHelper helper, DownloadItem item) {
        if (TextUtils.isEmpty(item.getDownloadURL())) {
            return false;
        }

        boolean success;
        if (hasData(helper, item.getDownloadURL())) {
            success = updateData(helper, item);
        } else {
            success = insertData(helper, item);
        }

        return success;
    }

    static boolean clearData(BaseDBHelper helper, long download_id) {
        int rows = 0;
        String whereClause = " " + col_download_id + " = ? ";
        String[] whereArgs = {String.valueOf(download_id)};
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            rows = db.delete(TABLE_NAME, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
            rows = 0;
        }
        return rows != 0;
    }
}
