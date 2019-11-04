package com.hss01248.imageloaderdemo.multi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.imagepipeline.systrace.FrescoSystrace;
import com.hss01248.adapter.SuperRvAdapter;
import com.hss01248.adapter.SuperRvHolder;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.imageloaderdemo.BigImageActy;
import com.hss01248.imageloaderdemo.R;

import java.util.List;

/**
 * Created by huangshuisheng on 2017/9/28.
 */

public class RcvHolder extends SuperRvHolder<String,Activity> {
    //public ImageView imageView;
    private int imageSize;
    private int columnNumber;
    public RcvHolder(View itemView) {
        super(itemView);
        this.rootView = itemView;
        //imageView = (ImageView) itemView.findViewById(R.id.item_iv);

    }

    @Override
    protected void findViewsById(View view) {

    }

    public RcvHolder setColumnNum(int columnNumber){
        this.columnNumber = columnNumber;
        WindowManager wm = (WindowManager) itemView.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = widthPixels / columnNumber;
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if(params==null){
            params = new ViewGroup.LayoutParams(imageSize,imageSize);
        }else {
            params.height = imageSize;
            params.width = imageSize;
        }
        itemView.setLayoutParams(params);
        return this;
    }

    @Override
    public void assignDatasAndEvents(Activity context, String data, int position, boolean isLast, boolean isListViewFling, List datas, SuperRvAdapter superRecyAdapter) {
        super.assignDatasAndEvents(context, data, position, isLast, isListViewFling, datas, superRecyAdapter);
        if(position == 3){
            Debug.startMethodTracing("imageloader");
        }

        //loadByGlide(context,data);

        ImageLoader.with(context)
                //.widthHeight(imageSize,imageSize)
                .url(data)
                //.blur(5)
                .defaultErrorRes(true)
                //.loadingDefault()
                //.scale(ScaleMode.CENTER_CROP)
                //.rectRoundCornerTop(5,0)
                .defaultPlaceHolder(true)
                //.rectRoundCorner(5,0)
                .into(itemView);

        if(position == 3){
            Debug.stopMethodTracing();
        }
    }

    @Override
    public void assignDatasAndEvents(final Activity context, final String data) {
        super.assignDatasAndEvents(context, data);








        /*itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,BigImageActy.class);
                intent.putExtra("url",data);
                context.startActivity(intent);
            }
        });*/
    }

    private void loadByGlide(Activity context, String data) {
        Glide.with(context).load(data)
                .placeholder(R.drawable.im_item_list_opt)
                .error(R.drawable.im_item_list_opt_error)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into((ImageView) itemView);
    }
}
