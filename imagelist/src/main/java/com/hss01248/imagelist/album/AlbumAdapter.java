package com.hss01248.imagelist.album;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ScreenUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.imagelist.R;

import java.util.List;

/**
 * time:2019/11/12
 * author:hss
 * desription:
 */
public class AlbumAdapter extends BaseQuickAdapter<Album, BaseViewHolder> implements SectionTitleProvider {
    public AlbumAdapter(int layoutResId, @Nullable List<Album> data) {
        super(layoutResId, data);
    }

    public AlbumAdapter(@Nullable List<Album> data) {
        super(data);
    }

    public AlbumAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final Album item) {
        helper.getView(R.id.item_iv).setTag(R.id.item_iv, item);
        ImageView imageView = helper.getView(R.id.item_iv);
        imageView.setAdjustViewBounds(false);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.height = (ScreenUtils.getAppScreenWidth() - ImageListView.dividerSize) / ImageListView.count();
        imageView.setLayoutParams(params);

        ImageLoader.with(helper.itemView.getContext())
                .load(item.cover)
                .scale(ScaleMode.CENTER_CROP)
                .defaultPlaceHolder(true)
                //.loading(R.drawable.iv_loading_trans)
                .error(R.drawable.im_item_list_opt_error)
                .into(imageView);
        helper.setText(R.id.tv_info, item.name);
        helper.getConvertView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageMediaCenterUtil.showViewAsActivity(v.getContext(), new IViewInit() {
                    @Override
                    public View init(Activity activity) {
                        ImageListView listView = new ImageListView(activity);
                        //ImageMediaCenterUtil.showViewAsDialog(listView);
                        listView.showImagesInAlbum(item);
                        return listView;
                    }
                });
            }
        });


    }


    @Override
    public String getSectionTitle(int position) {
        return position + "";
    }
}
