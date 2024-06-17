package com.hss01248.media.localvideoplayer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Keep;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;

import java.util.List;



@Keep
public class VideoPlayUtil {


    public static void setUrlInterceptor(IUrlInterceptor urlInterceptor) {
        VideoPlayUtil.urlInterceptor = urlInterceptor;
    }

    static IUrlInterceptor urlInterceptor;

    public static void preview(Context context, String pathOrUri){
        startPreview(context,pathOrUri,false,true);
    }
    /**
     *
     * @param pathOrUri
     * @param useThirdPartyPlayer
     * @param dismissPageWhenFinishPlay
     */
    public static void startPreview(Context context, String pathOrUri,
                                    boolean useThirdPartyPlayer, boolean dismissPageWhenFinishPlay){
        //EXOPlayer内核，支持格式更多
        //PlayerFactory.setPlayManager(IjkPlayerManager.class);

        config(pathOrUri);
        if(useThirdPartyPlayer){
            //todo uri抛到外部
            playByOther(context, pathOrUri);
            return;
        }
        Intent intent = new Intent(context,PictureVideoPlayByGSYActivity.class);
        intent.putExtra(PictureVideoPlayByGSYActivity.PATH,pathOrUri);
        intent.putExtra(PictureVideoPlayByGSYActivity.TAG_DISMISSPAGEWHENFINISHPLAY,dismissPageWhenFinishPlay);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * https://github.com/CarGuo/GSYVideoPlayer/issues/1757  遇到的问题集合
     * 大问题: 网速远远低于直接http下载该在当前网络能在达到的网速
     * 缓存大小的设置: https://github.com/CarGuo/GSYVideoPlayer/issues/3559
     *
     * proxyCacheManager内部使用的是一个代理服务器: HttpProxyCacheServer
     */
    private static void config(String urlPath) {
        //EXOPlayer内核，支持格式更多,但退出崩溃. ijk播放后退出也崩溃
        //PlayerFactory.setPlayManager(Exo2PlayerManager.class);
//系统内核模式: 硬件解码,只有这个播放4k才不卡,其他软件解码都tm卡
        /*if(urlPath.contains("VID_") || urlPath.contains("PXL_")){
            PlayerFactory.setPlayManager(SystemPlayerManager.class);
        }else {
            PlayerFactory.setPlayManager(IjkPlayerManager.class);
        }*/

        if(urlPath.endsWith(".m3u8")  ){
            //不稳定,容易崩溃
           // PlayerFactory.setPlayManager(Exo2PlayerManager.class);
            PlayerFactory.setPlayManager(SystemPlayerManager.class);
        }else {
            PlayerFactory.setPlayManager(SystemPlayerManager.class);
        }

//ijk内核，默认模式
       // PlayerFactory.setPlayManager(IjkPlayerManager.class);
//aliplay 内核，默认模式
       // PlayerFactory.setPlayManager(AliPlayerManager.class);


//exo缓存模式，支持m3u8，只支持exo
        //CacheFactory.setCacheManager(ExoPlayerCacheManager.class);
//代理缓存模式，支持所有模式，不支持m3u8等，默认
        CacheFactory.setCacheManager(ProxyCacheManager.class);
        ProxyCacheManager.DEFAULT_MAX_SIZE = 2000 * 1024 * 1024;

        //https://github.com/CarGuo/GSYVideoPlayer/issues/3351  预先缓存视频
        //exoplayer自定义MediaSource
     /*   ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
            @Override
            public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
                //可自定义MediaSource
                return null;
            }
        });*/
    }

    public static void playList( List<String> sources, int currentPosition){
        //EXOPlayer内核，支持格式更多
        config(sources.get(currentPosition));
        //PlayerFactory.setPlayManager(IjkPlayerManager.class);
        //PictureVideoPlayByGSYActivity.setVideos(sources);
       /* Intent intent = new Intent(context,PictureVideoPlayByGSYActivity.class);
        intent.putExtra(PictureVideoPlayByGSYActivity.PATH,sources.get(currentPosition));
        intent.putExtra(PictureVideoPlayByGSYActivity.POSITION,currentPosition);
        intent.putExtra(PictureVideoPlayByGSYActivity.IS_VIEW_LIST,true);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);*/
        //playByOther(context,sources.get(currentPosition));
        PictureVideoPlayByGSYActivity.start(sources.get(currentPosition),sources,currentPosition,false);
    }

    public static void playByOther(Context context,String pathOrUri) {
        try {
            if(urlInterceptor != null){
                pathOrUri = urlInterceptor.getUrlWithAuth(pathOrUri);
            }

            Log.w("play video",pathOrUri);
            Uri uri = Uri.parse(pathOrUri);
            Intent i = new Intent(Intent.ACTION_VIEW);
           /* ComponentName comp = new ComponentName("com.mxtech.videoplayer.pro","com.mxtech.videoplayer.pro.ActivityScreen");
            i.setComponent(comp);*/
            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            preferMxPlayer(i,uri);
            i.setDataAndType(uri, "video/*");
            context.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
            //去下载:
            //https://soft.shouji.com.cn/down/22398.html
        }

    }

    static boolean hasInstalledMxPlayerPro;
    public static void preferMxPlayer(Intent intent, Uri uri) {
        /*if(hasInstalledMxPlayerPro){
            hasInstalledMxPlayerPro = true;
            intent.setPackage("com.mxtech.videoplayer.pro");
        }*/
        long cost = System.currentTimeMillis();
        //看有没有安装
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "video/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.setPackage("com.mxtech.videoplayer.pro");
        List<ResolveInfo> resolves = Utils.getApp().getPackageManager()
                .queryIntentActivities(i, 0);
        boolean hasInstalled = resolves != null && !resolves.isEmpty();
        //耗时3ms. 不用缓存
        LogUtils.d("是否安装",resolves,hasInstalled,(System.currentTimeMillis() - cost)+"ms");
        if(!hasInstalled){
            return;
        }
        if(uri.toString().startsWith("http")){
            return;
        }
        hasInstalledMxPlayerPro = true;
        intent.setPackage("com.mxtech.videoplayer.pro");

    }

}
