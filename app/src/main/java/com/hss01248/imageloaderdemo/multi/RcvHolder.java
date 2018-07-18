package com.hss01248.imageloaderdemo.multi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


import com.hss01248.adapter.SuperRvHolder;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.imageloaderdemo.BigImageActy;
import com.hss01248.imageloaderdemo.R;

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
    public void assignDatasAndEvents(final Activity context, final String data) {
        super.assignDatasAndEvents(context, data);

        ImageLoader.with(context)
            .widthHeight(imageSize,imageSize)
            .url(data)
            .placeHolder(R.drawable.imageloader_placeholder_125,true,ScaleMode.CENTER_INSIDE)
            .loadingDefault()
            .error(R.drawable.imageloader_failure_image_104,ScaleMode.CENTER_INSIDE)
            .into(itemView);


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,BigImageActy.class);
                intent.putExtra("url",data);
                context.startActivity(intent);
            }
        });
    }
}
