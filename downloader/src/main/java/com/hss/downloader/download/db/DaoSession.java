package com.hss.downloader.download.db;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.SubFolderCount;

import com.hss.downloader.download.db.DownloadInfoDao;
import com.hss.downloader.download.db.SubFolderCountDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig downloadInfoDaoConfig;
    private final DaoConfig subFolderCountDaoConfig;

    private final DownloadInfoDao downloadInfoDao;
    private final SubFolderCountDao subFolderCountDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        downloadInfoDaoConfig = daoConfigMap.get(DownloadInfoDao.class).clone();
        downloadInfoDaoConfig.initIdentityScope(type);

        subFolderCountDaoConfig = daoConfigMap.get(SubFolderCountDao.class).clone();
        subFolderCountDaoConfig.initIdentityScope(type);

        downloadInfoDao = new DownloadInfoDao(downloadInfoDaoConfig, this);
        subFolderCountDao = new SubFolderCountDao(subFolderCountDaoConfig, this);

        registerDao(DownloadInfo.class, downloadInfoDao);
        registerDao(SubFolderCount.class, subFolderCountDao);
    }
    
    public void clear() {
        downloadInfoDaoConfig.clearIdentityScope();
        subFolderCountDaoConfig.clearIdentityScope();
    }

    public DownloadInfoDao getDownloadInfoDao() {
        return downloadInfoDao;
    }

    public SubFolderCountDao getSubFolderCountDao() {
        return subFolderCountDao;
    }

}
