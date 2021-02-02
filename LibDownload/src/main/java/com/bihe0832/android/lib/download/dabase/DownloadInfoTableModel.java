package com.bihe0832.android.lib.download.dabase;

import android.content.ContentValues;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.bihe0832.android.lib.download.DownloadItem;
import com.bihe0832.android.lib.download.DownloadStatus;
import com.bihe0832.android.lib.download.core.DownloadManager;
import com.bihe0832.android.lib.download.core.list.DownloadTaskList;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.sqlite.BaseDBHelper;
import com.bihe0832.android.lib.sqlite.BaseTableModel;
import com.bihe0832.android.lib.utils.apk.APKUtils;
import kotlin.jvm.Synchronized;

/**
 * DownloadInfoTableModel
 *
 * @author hardyshi code@bihe0832.com Created on 2020/6/12.
 */
public class DownloadInfoTableModel extends BaseTableModel {

    public static final String col_id = "id";
    public static final String col_download_id = "download_id";
    public static final String col_download_extraInfo = "download_extra_info";
    public static final String col_download_extrakey = "download_extra_key";
    public static final String col_download_package = "download_package";
    public static final String col_download_url = "download_url";
    public static final String col_download_icon_url = "download_icon";
    public static final String col_download_title = "download_title";
    public static final String col_download_length = "download_length";
    public static final String col_download_version = "download_version";
    public static final String col_download_file = "download_file";
    public static final String col_download_status = "download_status";
    public static final String col_download_temp_file = "download_temp_file";
    static final String TABLE_NAME = "download_info";
    static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS ["
            + TABLE_NAME
            + "] ("
            + "[" + col_id + "] INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "[" + col_download_id + "] NVARCHAR(128)  NULL,"
            + "[" + col_download_extraInfo + "] NVARCHAR(128)  NULL,"
            + "[" + col_download_extrakey + "] NVARCHAR(128)  NULL,"
            + "[" + col_download_url + "] VARCHAR(256)  NULL,"
            + "[" + col_download_package + "] NVARCHAR(128)  NULL,"
            + "[" + col_download_title + "] VARCHAR(256)  NULL,"
            + "[" + col_download_icon_url + "] VARCHAR(256)  NULL,"
            + "[" + col_download_length + "] VARCHAR(256)  NULL,"
            + "[" + col_download_version + "] VARCHAR(256)  NULL,"
            + "[" + col_download_status + "] VARCHAR(256) NULL,"
            + "[" + col_download_temp_file + "] VARCHAR(256)  NULL,"
            + "[" + col_download_file + "] VARCHAR(256) NULL"
            + ")";
    static final String TABLE_DROP_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;

    static int deleteAll(BaseDBHelper helper) {
        return deleteAll(helper, TABLE_NAME);
    }

    private static ContentValues data2CV(DownloadItem item) {
        ContentValues cv = new ContentValues();
        putValues(cv, col_download_id, item.getDownloadID());
        putValues(cv, col_download_extraInfo, item.getExtraInfo());
        putValues(cv, col_download_extrakey, item.getActionKey());
        putValues(cv, col_download_url, item.getDownloadURL());
        putValues(cv, col_download_package, item.getPackageName());
        putValues(cv, col_download_title, item.getDownloadTitle());
        putValues(cv, col_download_icon_url, item.getDownloadIcon());
        putValues(cv, col_download_length, item.getFileLength());
        putValues(cv, col_download_version, item.getVersionCode());
        putValues(cv, col_download_status, item.getStatus());
        putValues(cv, col_download_temp_file, item.getTempFilePath());
        putValues(cv, col_download_file, item.getFinalFilePath());
        return cv;
    }

    private static DownloadItem cv2data(Cursor cursor) {
        DownloadItem item = new DownloadItem();
        item.setExtraInfo(getStringByName(cursor, col_download_extraInfo));
        item.setActionKey(getStringByName(cursor, col_download_extrakey));
        item.setDownloadURL(getStringByName(cursor, col_download_url));
        item.setPackageName(getStringByName(cursor, col_download_package));
        item.setDownloadTitle(getStringByName(cursor, col_download_title));
        item.setDownloadIcon(getStringByName(cursor, col_download_icon_url));
        item.setFileLength(getLongByName(cursor, col_download_length));
        item.setVersionCode(getLongByName(cursor, col_download_version));
        int status = getIntByName(cursor, col_download_status);
        if (status == DownloadStatus.STATUS_DOWNLOADING || status == DownloadStatus.STATUS_DOWNLOAD_STARTED
                || status == DownloadStatus.STATUS_DOWNLOAD_WAITING) {
            item.setStatus(DownloadStatus.STATUS_DOWNLOAD_PAUSED);
        } else if (status == DownloadStatus.STATUS_HAS_DOWNLOAD || status == DownloadStatus.STATUS_DOWNLOAD_SUCCEED) {
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
        item.setTempFilePath(getStringByName(cursor, col_download_temp_file));
        item.setFinalFilePath(getStringByName(cursor, col_download_file));
        return item;
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
                    if (FileUtils.INSTANCE.checkFileExist(item.getFinalFilePath())) {
                        item.setFinished(item.getFileLength());
                        item.setStatus(DownloadStatus.STATUS_DOWNLOAD_SUCCEED);
                    }
                    long id = getLongByName(cursor, col_id);
                    ZLog.d("数据库信息：id:" + id + "info :" + item.toString());
                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        String whereClause = " `" + col_download_url + "` = ? ";
        String[] whereArgs = new String[]{item.getDownloadURL()};
        int rows = helper.update(TABLE_NAME, values, whereClause, whereArgs);
        return (rows != 0);
    }

    static boolean hasData(BaseDBHelper helper, String url) {
        String[] columns = null;
        String selection = " " + col_download_url + " = ? ";
        String[] selectionArgs = {url};
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
        if (cursor.getCount() < 1) {
            return null;
        } else {
            try {
                cursor.moveToFirst();
                return cv2data(cursor);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return null;
    }

    static DownloadItem getDownloadInfo(BaseDBHelper helper, String url) {
        String selection = " " + col_download_url + " = ? ";
        String[] selectionArgs = {String.valueOf(url)};
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
