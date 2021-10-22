package com.hss.downloader.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.blankj.utilcode.util.Utils;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss.downloader.download.db.DaoMaster;
import com.hss.downloader.download.db.DaoSession;
import com.hss.downloader.download.db.DownloadInfoDao;
import com.hss.downloader.download.db.SubFolderCountDao;

public class DownloadInfoUtil {

    static DownloadInfoDao dao;
    static SubFolderCountDao folderCountDao;
    static Context context;
    public static DownloadInfoDao getDao(){
        if(dao == null){
            dao = getDaoSession().getDownloadInfoDao();
        }
        return dao;
    }

    public static SubFolderCountDao getFolderCountDao(){
        if(folderCountDao == null){
            folderCountDao = getDaoSession().getSubFolderCountDao();
        }
        return folderCountDao;
    }

    static void init(Context context) {
        Context context2 = context;
        if(  XXPermissions.isGranted(context,Permission.MANAGE_EXTERNAL_STORAGE)){
            context2 = new MyDBContext(context);
        }
        DaoMaster.OpenHelper helper = new MySQLiteOpenHelper(context2, "imgdownload.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    private volatile static DaoSession daoSession;

     static DaoSession getDaoSession() {
        if (daoSession == null) {
            synchronized (DownloadInfoUtil.class) {
                if (daoSession == null) {
                    if(context ==  null){
                        context = Utils.getApp();
                    }
                    init(context);
                }
            }
        }
        return daoSession;
    }
}
