package com.hss.downloader.list;

import com.blankj.utilcode.util.LogUtils;
import com.hss.downloader.download.DownloadInfo;
import com.hss.utils.enhance.api.MyCommonCallback;

import com.hss01248.refresh_loadmore.ILoadData;
import com.hss01248.refresh_loadmore.PagerDto;
import com.hss.downloader.download.DownloadInfoUtil;

/**
 * @Despciption todo
 * @Author hss
 * @Date 10/01/2023 19:54
 * @Version 1.0
 */
public class LoadDataByHistoryDb implements ILoadData<DownloadInfo> {

    public LoadDataByHistoryDb() {

    }

    @Override
    public void queryData(PagerDto<DownloadInfo> pager, MyCommonCallback<PagerDto<DownloadInfo>> callback) {
        try {
            PagerDto<DownloadInfo> browserHistoryInfoPagerDto = DownloadInfoUtil.loadByPager(pager);
            callback.onSuccess(browserHistoryInfoPagerDto);
        }catch (Throwable throwable){
            LogUtils.w(throwable);
            callback.onError(throwable.getClass().getSimpleName(),throwable.getMessage(),throwable);
        }
    }

    @Override
    public boolean deleteData(DownloadInfo data, int position) {
        DownloadInfoUtil.getDaoSession().getDownloadInfoDao().delete(data);
        return true;
    }


}
