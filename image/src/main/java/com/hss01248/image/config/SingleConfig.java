package com.hss01248.image.config;

import android.content.Context;
import android.view.View;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class SingleConfig {

    public Context context;


    public String url;
    public String filePath;


    public View target;



    public int width;
    public int height;

    public boolean needBlur = false;//是否需要模糊





    public SingleConfig(Context context) {
        this.context = context;
    }

    public SingleConfig url(String url){
        this.url = url;
        return this;
    }
    public SingleConfig file(String filePath){
        this.filePath = filePath;
        return this;
    }
    public SingleConfig fileDescriptor(String filePath){
        this.filePath = filePath;
        return this;
    }


    /**
     * 最终加载的动作
     * @param view
     * @return
     */
    public SingleConfig into(View view){
        this.target = view;
        return this;
    }


}
