package com.hss01248.imagelist.album;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
public class AlbumImgAdapter extends BaseQuickAdapter<Image, BaseViewHolder> implements SectionTitleProvider {
    public AlbumImgAdapter(int layoutResId, @Nullable List<Image> data) {
        super(layoutResId, data);
    }

    public AlbumImgAdapter(@Nullable List<Image> data) {
        super(data);
    }

    public AlbumImgAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final Image item) {
        helper.getView(R.id.item_iv).setTag(R.id.item_iv,item);
        helper.addOnClickListener(R.id.item_iv);
        ImageLoader.with(helper.itemView.getContext())
                .url(item.path)
                .loading(R.drawable.iv_loading_trans)
                .error(R.drawable.im_item_list_opt_error)
                .into(helper.getView(R.id.item_iv));
        if(item.width != 0){
            helper.setText(R.id.tv_info,item.width+"x"+item.height+","+ MyUtil.formatFileSize(item.fileSize));
        }else {
            int[] wh = MyUtil.getImageWidthHeight(item.path);
            item.width = wh[0];
            item.height = wh[1];
            item.fileSize = new File(item.path).length();
            helper.setText(R.id.tv_info,item.width+"x"+item.height+","+ MyUtil.formatFileSize(item.fileSize));
        }

    }


    @Override
    public String getSectionTitle(int position) {
        return position+"";
    }
}
