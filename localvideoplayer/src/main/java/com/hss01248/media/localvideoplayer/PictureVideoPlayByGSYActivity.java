package com.hss01248.media.localvideoplayer;

import static android.view.View.GONE;
import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_AUTO_COMPLETE;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hss01248.bitmap_saver.BitmapSaveUtil;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.GSYStateUiListener;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PictureVideoPlayByGSYActivity extends GSYBaseActivityDetail2<StandardGSYVideoPlayer> {
    LocalVideoPlayer detailPlayer;
    String videoPath;
    int position;

    public static final String PATH = "path";
    public static final String IS_VIEW_LIST = "isViewList";
    public static final String POSITION = "position";
    public static final String TAG_DISMISSPAGEWHENFINISHPLAY = "dismissPageWhenFinishPlay";

    boolean dismissPageWhenFinishPlay;
    boolean isViewList;

    public static List<String> getVideos() {
        return videos;
    }

    public static void setVideos(List<String> videos) {
        PictureVideoPlayByGSYActivity.videos.clear();
        PictureVideoPlayByGSYActivity.videos.addAll(videos);
    }

    static List<String> videos = new ArrayList<>();

    public static void start(String path,List<String> videos,int position,boolean dismissPageWhenFinishPlay){
        if(videos != null && !videos.isEmpty()){
            setVideos(videos);
        }
        Intent intent = new Intent(ActivityUtils.getTopActivity(),PictureVideoPlayByGSYActivity.class);
        intent.putExtra(PictureVideoPlayByGSYActivity.PATH,path);
        intent.putExtra(PictureVideoPlayByGSYActivity.TAG_DISMISSPAGEWHENFINISHPLAY,dismissPageWhenFinishPlay);
        intent.putExtra(PictureVideoPlayByGSYActivity.IS_VIEW_LIST,videos!=null && !videos.isEmpty());
        intent.putExtra(PictureVideoPlayByGSYActivity.POSITION,position);
        ActivityUtils.getTopActivity().startActivity(intent);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoPath = getIntent().getStringExtra(PATH);
        dismissPageWhenFinishPlay = getIntent().getBooleanExtra(TAG_DISMISSPAGEWHENFINISHPLAY,false);
        position = getIntent().getIntExtra(POSITION,0);
        isViewList = getIntent().getBooleanExtra(IS_VIEW_LIST,false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_detail_player);
        detailPlayer = (LocalVideoPlayer) findViewById(R.id.detail_player);
        detailPlayer.setActivity(this);
        //detailPlayer.setVideoList(isViewList);
        //增加title
        //detailPlayer.getTitleTextView().setVisibility(View.GONE);
        //detailPlayer.getBackButton().setVisibility(View.GONE);

        initVideoBuilderMode();
//外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, detailPlayer);
//初始化不打开外部的旋转
        orientationUtils.setEnable(true);
        BitmapSaveUtil.setPrefix(theName(videoPath));
        try {
            //detailPlayer.getGSYVideoManager().start();
            //detailPlayer.getStartButton().setVisibility(View.GONE);
            detailPlayer.setDismissControlTime(2500);
            detailPlayer.startPlayLogic();
            //detailPlayer.setFullHideStatusBar(false);
        } catch (Throwable e) {
            e.printStackTrace();
        }


        BarUtils.setStatusBarColor(this,Color.TRANSPARENT);

        getWindow().setNavigationBarColor(Color.BLACK);
        BarUtils.setStatusBarVisibility(this,true);
        BarUtils.setNavBarVisibility(this,false);
        //BarUtils.setNavBarLightMode(getWindow(),false);
        //BarUtils.setNavBarVisibility(this,false);


    }

    private void hideUI() {
        ReflectUtils.reflect(detailPlayer).method("hideAllWidget").get();
        detailPlayer.bottomLl.setVisibility(GONE);
        detailPlayer.llTop.setVisibility(GONE);
        detailPlayer.ivSnapshot.setVisibility(GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
           // hideSystemUI();


        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

   boolean  onPlayNext(){
       Log.w("click","onPlayNext:"+detailPlayer.toString());
       //Toast.makeText(getApplicationContext(),"onPlayNext:"+detailPlayer.toString(),Toast.LENGTH_SHORT).show();

        if(!isViewList){
            return false;
        }
        if(position >= videos.size()-1){
            return false;
        }
        position++;
       SPStaticUtils.put(EncryptUtils.encryptMD5ToString(currentUrl)+"_progress",(int)detailPlayer.getCurrentPlayer().getCurrentPositionWhenPlaying());
        videoPath = videos.get(position);

       playVideo(videoPath,position);
        return true;
    }

    void onPlayPre(){
        Log.w("click","onPlayPre:"+detailPlayer.toString());
        //Toast.makeText(getApplicationContext(),"onPlayPre:"+detailPlayer.toString(),Toast.LENGTH_SHORT).show();
        if(!isViewList){
            return;
        }
        if(position <= 0){
            return;
        }
        position--;
        SPStaticUtils.put(EncryptUtils.encryptMD5ToString(currentUrl)+"_progress",(int)detailPlayer.getCurrentPlayer().getCurrentPositionWhenPlaying());
        videoPath = videos.get(position);

        playVideo(videoPath,position);
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    String currentUrl= "";
    private void playVideo(String url ,int position) {
        String uri = parseUrl(url);



        // 播放一个视频结束后，直接调用此方法，切换到下一个
        // ?（问题：全屏播放的时候，播放结束了，自动回来调用在这个方法想播放下一个，只有声音，但画面没改变，黑的）
        //detailPlayer.release();
        Map<String,String> headers = new HashMap<>();
        if(VideoPlayUtil.urlInterceptor != null){
            headers.putAll(VideoPlayUtil.urlInterceptor.addHeaders(uri));
        }
      /*  String host = Uri.parse(url).getHost();
        if(HttpAuthInterceptor.getAuthMap().containsKey(host)){
            headers.put("Authorization",HttpAuthInterceptor.getAuthMap().get(host));
        }*/
        currentUrl = uri;
        int playPosition = SPStaticUtils.getInt(EncryptUtils.encryptMD5ToString(currentUrl)+"_progress",0);
        LogUtils.d("progress start position "+ currentUrl+", "+ playPosition);
        getGSYVideoOptionBuilder().setUrl(uri)
                .setVideoTitle(getNameFromPath(url))
                //.setThumbImageView()
                //.setPlayPosition(position)
                .setSeekOnStart(playPosition)
                .setMapHeadData(headers)
                .build(detailPlayer.getCurrentPlayer());
        //getGSYVideoOptionBuilder().build(detailPlayer);
        detailPlayer.getCurrentPlayer().startPlayLogic();
        BitmapSaveUtil.setPrefix(theName(url));
        //detailPlayer.setCurrentName(theName(url));
        ThreadUtils.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideUI();
            }
        },500);

    }



    private String parseUrl(String url) {
        String uri = url;
        if(url.startsWith("/storage/")){
            //todo 映射,创建临时缓存文件用于播放,退出app时删除
            uri = Uri.fromFile(new File(uri)).toString();
        }else if(url.startsWith("content")){
            uri = url;
        }
        if(VideoPlayUtil.urlInterceptor != null){
            //uri = VideoPlayUtil.urlInterceptor.getUrlWithAuth(url);
        }
        Log.w("parseUrl","on playVideo:"+uri);
        return uri;
    }


    @Override
    public StandardGSYVideoPlayer getGSYVideoPlayer() {
        return detailPlayer;
    }


    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //内置封面可参考SampleCoverVideo
       // ImageView imageView = new ImageView(this);
        //loadCover(imageView, url);
        String uri = parseUrl(videoPath);
        currentUrl = uri;
        Map<String,String> headers = new HashMap<>();
        if(VideoPlayUtil.urlInterceptor != null){
            headers.putAll(VideoPlayUtil.urlInterceptor.addHeaders(uri));
        }
        BitmapSaveUtil.setPrefix(currentUrl);
        int playPosition = SPStaticUtils.getInt(EncryptUtils.encryptMD5ToString(currentUrl)+"_progress",0);
        LogUtils.d("progress start position "+ currentUrl+", "+ playPosition);
        return new GSYVideoOptionBuilder()
                //.setThumbImageView(imageView)
                //.setUrl(url)
                .setUrl(uri)
                .setMapHeadData(headers)
                .setCacheWithPlay(false)
                .setShowFullAnimation(false)
                .setVideoTitle(getNameFromPath(videoPath))
        //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
                .setAutoFullWithSize(true)
               // .setPlayPosition(playPosition)
                .setSeekOnStart(playPosition)
                .setIsTouchWiget(true)
               // .setRotateViewAuto(false)
                //.setLockLand(false)
                .setShowPauseCover(false)
                //.setPlayPosition(position)
                .setStartAfterPrepared(true)
                .setShowFullAnimation(false)
                .setNeedLockFull(false)
                .setGSYStateUiListener(new GSYStateUiListener() {
                    @Override
                    public void onStateChanged(int state) {
                        if(state == CURRENT_STATE_AUTO_COMPLETE){
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(!isViewList){
                                        finish();
                                    }else {
                                       boolean success =  onPlayNext();
                                       if(!success){
                                           finish();
                                       }
                                    }


                                }
                            },300);
                        }
                    }
                })
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                        //开始播放了才能旋转和全屏
                        orientationUtils.setEnable(true);
                        isPlay = true;

                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);
                        Debuger.printfError("***** onQuitFullscreen **** " + objects[0]);//title
                        Debuger.printfError("***** onQuitFullscreen **** " + objects[1]);//当前非全屏player
                        if (orientationUtils != null) {
                            orientationUtils.backToProtVideo();
                        }
                        //hideSystemUI();
                    }
                })
                //.setThumbPlay(true)
                .setSeekRatio(1);
    }

    @Override
    public void onAutoComplete(String url, Object... objects) {
        super.onAutoComplete(url, objects);
        //onPlayNext()
       // detailPlayer.getCurrentPlayer().startPlayLogic();
    }

    private String getNameFromPath(String videoPath) {

        String name = position+"/"+videos.size()+", ";
        videoPath = URLDecoder.decode(videoPath);
        if(videoPath.contains("/")){
            name = name+ theName(videoPath);
        }else {
            name = name+videoPath;
        }
        name = URLDecoder.decode(name);
        return name;
    }

    //todo xxxx
    private String theName(String uri) {
        String original = uri;
        if(TextUtils.isEmpty(uri)){
            return "";
        }
        uri = URLDecoder.decode(uri);
        if(uri.contains("?")){
            uri = uri.substring(0,uri.lastIndexOf("?"));
        }
        if(uri.contains("/")){
            uri = uri.substring(uri.lastIndexOf("/")+1);
        }
        if(uri.contains(".")){
            uri = uri.substring(0,uri.lastIndexOf("."));
        }
        if("xxy".equals(uri)){
            try{
                uri = ReflectUtils.reflect("com.hss01248.media.mymediastore.http.HttpSearchParser2")
                        .method("getInfo",original)
                        .method("getFileName")
                        .get();
            }catch (Throwable throwable){
                LogUtils.w(throwable);
                uri = uri+"-"+throwable.getMessage();
            }

        }
        return uri;
    }

    @Override
    public void clickForFullScreen() {

    }

    @Override
    protected void onPause() {
        //禁用后台跳转:
        super.onPause();
        SPStaticUtils.put(EncryptUtils.encryptMD5ToString(currentUrl)+"_progress",(int)detailPlayer.getCurrentPlayer().getCurrentPositionWhenPlaying());
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //记录进度和亮度,声音
        SPStaticUtils.put(EncryptUtils.encryptMD5ToString(currentUrl)+"_progress",(int)detailPlayer.getCurrentPlayer().getCurrentPositionWhenPlaying());
        finish();
        videos.clear();
    }

    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }
}
