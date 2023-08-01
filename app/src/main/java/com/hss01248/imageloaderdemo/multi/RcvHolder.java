package com.hss01248.imageloaderdemo.multi;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.hss01248.adapter.SuperRvAdapter;
import com.hss01248.adapter.SuperRvHolder;
import com.hss01248.bigimageviewpager.LargeImageViewer;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.imageloaderdemo.R;

import java.util.List;

/**
 * Created by huangshuisheng on 2017/9/28.
 */

public class RcvHolder extends SuperRvHolder<String, Activity> {
    //public ImageView imageView;
    private int imageSize;
    private int columnNumber;
    //private RoundedImageView roundedImageView2;
    // private ImageLoaderRoundImageView roundImageView;
    private ImageView roundImageView3;

    public RcvHolder(View itemView) {
        super(itemView);
        this.rootView = itemView;
        //roundImageView =  itemView.findViewById(R.id.item_iv);
        //roundedImageView2 = itemView.findViewById(R.id.iv_round2);
        roundImageView3 = itemView.findViewById(R.id.iv_round3);
        //imageView = (ImageView) itemView.findViewById(R.id.item_iv);

    }

    @Override
    protected void findViewsById(View view) {

    }

    public RcvHolder setColumnNum(int columnNumber) {
        this.columnNumber = columnNumber;
        WindowManager wm = (WindowManager) itemView.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = widthPixels / columnNumber;
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(imageSize, imageSize);
        } else {
            params.height = imageSize;
            params.width = imageSize;
        }
        itemView.setLayoutParams(params);
        return this;
    }

    @Override
    public void assignDatasAndEvents(Activity context, final String data, int position, boolean isLast, boolean isListViewFling, List datas, SuperRvAdapter superRecyAdapter) {
        super.assignDatasAndEvents(context, data, position, isLast, isListViewFling, datas, superRecyAdapter);


        //loadByGlide(context,data,position);

        //roundImageView.setCornerRadius(20,20,0,0);
        //roundImageView.setBorderWidth(2);
        //roundImageView.setBorderColor(Color.BLUE);
        if (position == 3) {
            //Debug.startMethodTracing("imageloaderfresco2");
        }
        ImageLoader.with(context)
                //.widthHeightByPx(360,360)
                .url(data)
                .scale(ScaleMode.CENTER_CROP)
                .rectRoundCorner(10, Color.WHITE)
                .blur(10)
                //.asCircle()
                //.border(5,R.color.colorPrimary)
                .placeHolder(R.drawable.imageloader_placeholder_125, true, ScaleMode.CENTER_INSIDE)
                .error(R.drawable.imageloader_failure_image_104, ScaleMode.CENTER_INSIDE)
                /*.asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        XLog.i(MyUtil.printBitmap(bitmap));
                        roundImageView3.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onFail(Throwable e) {

                    }
                });*/
                //.loading(R.drawable.iv_loading_trans)
                .into(roundImageView3);
        if (position == 3) {
            //Debug.stopMethodTracing();
        }
       /* ImageLoader.with(context)
                //.widthHeightByPx(360,360)
                .url(data+"?t=4")
                //.scale(ScaleMode.CENTER_CROP)
                //.blur(2)
                .defaultErrorRes(true)
                .loading(R.drawable.iv_loading_trans)
                .into(roundedImageView2);*/

        /*ImageLoader.with(context)
                //.widthHeightByPx(360,360)
                .url(data+"?t=5")
                //.scale(ScaleMode.CENTER_CROP)
                //.blur(2)
                .defaultErrorRes(true)
                .loading(R.drawable.iv_loading_trans)
                .into(roundImageView);*/

                /*.asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        Log.w("onsuccess", MyUtil.printBitmap(bitmap));
                        ((ImageView)itemView).setImageBitmap(bitmap);
                    }

                    @Override
                    public void onFail(Throwable e) {

                    }
                });*/


    }

    @Override
    public void assignDatasAndEvents(final Activity context, final String data) {
        super.assignDatasAndEvents(context, data);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LargeImageViewer.showInDialog(data);
            }
        });
    }


}
