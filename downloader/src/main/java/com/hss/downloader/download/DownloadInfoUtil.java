package com.hss.downloader.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss.downloader.download.db.DaoMaster;
import com.hss.downloader.download.db.DaoSession;
import com.hss.downloader.download.db.DownloadInfoDao;
import com.hss.downloader.download.db.SubFolderCountDao;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String getLeagalFileName(String name){
         if(TextUtils.isEmpty(name)){
             return "name-empty";
         }
        name = checkFileName(name);
         String suffix = "";
         if(name.contains(".")){
             suffix = name.substring(name.lastIndexOf("."));
         }

         name = name.substring(0,name.length() - suffix.length() -1);

         int maxLenght = 240- suffix.length() -2;
        //   //处理文件长度太长的情况
        //            //Linux文件名的长度限制是255个字节
        //            //windows下完全限定文件名必须少于260个字节，目录名必须小于248个字节。
         while (name.getBytes().length> maxLenght ){
             name = name.substring(0,name.length()-2);
             LogUtils.w("缩短文件名",name);
        }
       name = name + suffix;
        return name;
    }
    static final Pattern pattern =  Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
    /**
     * window操作系统文件名不能含有 ? “ ”/ \ < > * | :
     * mac操作系统文件名不能以.开头
     * linux和Mac基本一直，
     *
     * @param fileName
     * @return
     */
    public static String checkFileName(String fileName) {
        Matcher matcher = pattern.matcher(fileName);
        fileName = matcher.replaceAll(""); // 将匹配到的非法字符以空替换
        return fileName;
    }
}
