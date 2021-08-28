package com.hss01248.imagelist.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.blankj.utilcode.util.Utils;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss01248.imagelist.download.db.DaoMaster;
import com.hss01248.imagelist.download.db.DaoSession;
import com.hss01248.imagelist.download.db.DownloadInfoDao;

public class DownloadInfoUtil {

    static DownloadInfoDao dao;
    static Context context;
    public static DownloadInfoDao getDao(){
        if(dao == null){
            dao = getDaoSession().getDownloadInfoDao();
        }
        return dao;
    }

    static void init(Context context) {
        Context context2 = context;
        if(  XXPermissions.isGranted(context,Permission.MANAGE_EXTERNAL_STORAGE)){
            context2 = new MyDBContext(context);
        }
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context2, "imgdownload.db");
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
