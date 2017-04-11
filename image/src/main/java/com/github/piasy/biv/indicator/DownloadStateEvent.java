package com.github.piasy.biv.indicator;


/**
 * Created by Administrator on 2017/4/11 0011.
 */

public class DownloadStateEvent {
    public String url;
    public int type;//1-progress,2-finish,3-fail,4-start
    public int progress;

    public static final int STATE_PROGRESS =1;
    public static final int STATE_FINISH =2;
    public static final int STATE_FAIL =3;
    public static final int STATE_START =4;




    public DownloadStateEvent(String url, int type, int progress) {
        this.url = url;
        this.type = type;
        this.progress = progress;
    }
}
