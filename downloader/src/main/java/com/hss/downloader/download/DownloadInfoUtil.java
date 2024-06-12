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
import com.hss01248.refresh_loadmore.PagerDto;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.net.URLDecoder;
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

   public   static DaoSession getDaoSession() {
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

    /**
     * 请使用getLeagalFileName(String dir,String name),能够对同名文件重命名
     * @param name
     * @return
     */
    @Deprecated
    public static String getLeagalFileName(String name){
         if(TextUtils.isEmpty(name)){
             return "name-empty";
         }
        name = checkFileName(name);
         while (name.endsWith(".")){
             name = name.substring(0,name.length()-1);
         }
         String suffix = "";
         if(name.contains(".")){
             suffix = name.substring(name.lastIndexOf("."));
         }
         name = URLDecoder.decode(name);

        name = name.replaceAll(" ","_").replaceAll("\\+","_");
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

    public static String getLeagalFileName(String dir,String name){
        if(TextUtils.isEmpty(name)){
            return "name-empty";
        }
        name = checkFileName(name);
        while (name.endsWith(".")){
            name = name.substring(0,name.length()-1);
        }
        String suffix = "";
        if(name.contains(".")){
            suffix = name.substring(name.lastIndexOf("."));
        }
        try{
            name = URLDecoder.decode(name);
        }catch (Throwable throwable){
            LogUtils.w(name,throwable);
        }
        name = name.replaceAll(" ","_").replaceAll("\\+","_");
        name = name.substring(0,name.length() - suffix.length() -1);

        int maxLenght = 240- suffix.length() -2;
        //   //处理文件长度太长的情况
        //            //Linux文件名的长度限制是255个字节
        //            //windows下完全限定文件名必须少于260个字节，目录名必须小于248个字节。
        while (name.getBytes().length> maxLenght ){
            name = name.substring(0,name.length()-2);
            LogUtils.w("缩短文件名",name);
        }
       String  finalName = name + suffix;
        if(new File(dir,finalName).exists()){
            for (int i = 1; i < 50; i++) {
                String  finalName2 = name+"("+i+")" + suffix;
                if(!new File(dir,finalName2).exists()){
                    return finalName2;
                }
            }
        }

        return finalName;
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

    public static PagerDto<DownloadInfo> loadByPager(PagerDto pagerDto){
        QueryBuilder<DownloadInfo> builder = getDaoSession().getDownloadInfoDao().queryBuilder()
                //.where(DownloadInfoDao.Properties.IsCollect.eq(isCollect? 1: 0))
                .orderDesc(DownloadInfoDao.Properties.CreateTime)
                .limit(pagerDto.pageSize)
                .offset((int) pagerDto.offset);
        if(!TextUtils.isEmpty(pagerDto.searchText)){
            builder.whereOr(DownloadInfoDao.Properties.Name.like("%"+pagerDto.searchText+"%"),
                    DownloadInfoDao.Properties.Url.like("%"+pagerDto.searchText+"%"));
        }
        List<DownloadInfo> list = builder.list();
        PagerDto<DownloadInfo> pagerDto1 = new PagerDto<DownloadInfo>();
        pagerDto1.isLast = list.size() < pagerDto.pageSize;
        pagerDto1.datas = list;
        //在这里自动计算偏移,在界面里直接透传即可
        pagerDto1.offset = pagerDto.offset+ list.size();
        pagerDto1.pageSize = pagerDto.pageSize;
        return pagerDto1;
    }
}
