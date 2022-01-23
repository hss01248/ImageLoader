package com.hss01248.imagelist.album;

import com.hss.downloader.download.DownloadInfoUtil;
import com.hss.downloader.download.SubFolderCount;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class ImgDirUtil {

    public static File dealFolderCount(File dir, boolean hideFolder) {
        if(!dir.exists()){
            dir.mkdirs();
        }
        SubFolderCount load = DownloadInfoUtil.getFolderCountDao().load(dir.getAbsolutePath());
        if(load == null){
            load = new SubFolderCount();
            load.dirPath = dir.getAbsolutePath();
            load.count = 1;
            File subDir =  createSubDir(dir,1,hideFolder);
            DownloadInfoUtil.getFolderCountDao().insert(load);
            return subDir;
        }

        File subDir = createSubDir(dir,load.count,hideFolder);
        File[] list = subDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory();
            }
        });
        if(list != null && list.length > 3000){
            load.count = load.count +1;
            File  subDir2 = createSubDir(dir,load.count,hideFolder);
            DownloadInfoUtil.getFolderCountDao().update(load);
            return subDir2;
        }else {
            return subDir;
        }
    }

    private static File createSubDir(File dir, int count, boolean hideFolder) {
        dir = new File(dir,dir.getName()+count);
        if(!dir.exists()){
            dir.mkdirs();
        }
       // if(hideFolder){
            File hidden = new File(dir,".nomedia");
            if(!hidden.exists()){
                try {
                    hidden.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
       // }
        return dir;
    }
}
