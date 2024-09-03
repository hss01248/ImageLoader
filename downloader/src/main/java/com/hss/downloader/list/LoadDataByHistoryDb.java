package com.hss.downloader.list;

import com.blankj.utilcode.util.ThreadUtils;
import com.hss.downloader.download.DownloadInfo;
import com.hss.downloader.download.DownloadInfoUtil;
import com.hss.utils.enhance.api.MyCommonCallback;
import com.hss01248.refresh_loadmore.ILoadData;
import com.hss01248.refresh_loadmore.PagerDto;

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
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<PagerDto<DownloadInfo>>() {
            @Override
            public PagerDto<DownloadInfo> doInBackground() throws Throwable {
                return DownloadInfoUtil.loadByPager(pager);
            }

            @Override
            public void onSuccess(PagerDto<DownloadInfo> result) {
                callback.onSuccess(result);
                //LogUtils.d(result.pageIndex);
            }

            @Override
            public void onFail(Throwable throwable) {
                super.onFail(throwable);
                callback.onError(throwable.getClass().getSimpleName(),throwable.getMessage(),throwable);
            }
        });
    }

    @Override
    public boolean deleteData(DownloadInfo data, int position) {
        DownloadInfoUtil.getDaoSession().getDownloadInfoDao().delete(data);
        return true;
    }


}
