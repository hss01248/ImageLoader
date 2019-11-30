package com.hss01248.imagelist.album;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.image.interfaces.ImageListener;
import com.hss01248.imagelist.R;

import java.io.File;
import java.util.List;

/**
 * time:2019/11/12
 * author:hss
 * desription:
 */
public class ImgItemAdapter extends BaseQuickAdapter<String, BaseViewHolder> implements SectionTitleProvider {
    public ImgItemAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    public ImgItemAdapter(@Nullable List<String> data) {
        super(data);
    }

    public ImgItemAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final String item) {
        helper.getView(R.id.item_iv).setTag(R.id.item_iv,item);
        helper.addOnClickListener(R.id.item_iv);
        ImageLoader.with(helper.itemView.getContext())
                .url(item)
                .loading(R.drawable.iv_loading_trans)
                .error(R.drawable.im_item_list_opt_error)
                .setImageListener(new ImageListener() {
                    @Override
                    public void onSuccess(@NonNull Drawable drawable, @Nullable Bitmap bitmap, int bWidth, int bHeight) {
                        ImageLoader.getActualLoader().getFileFromDiskCache(item, new FileGetter() {
                            @Override
                            public void onSuccess(File file, int width, int height) {
                                if(item.equals(helper.getView(R.id.item_iv).getTag(R.id.item_iv))){
                                    helper.setText(R.id.tv_info,width+"x"+height+","+ MyUtil.formatFileSize(file.length()));
                                }

                            }

                            @Override
                            public void onFail(Throwable e) {

                            }
                        });
                    }

                    @Override
                    public void onFail(Throwable e) {

                    }
                })
                .into(helper.getView(R.id.item_iv));

        //helper.setText(R.id.tv_info,width+"x"+height+","+ MyUtil.formatFileSize(file.length()));



    }


    @Override
    public String getSectionTitle(int position) {
        return position+"";
    }
}
