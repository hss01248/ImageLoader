package com.hss01248.imagelist.album;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.image.interfaces.ImageListener;
import com.hss01248.imagelist.R;

import java.io.File;
import java.io.IOException;
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
        helper.getView(R.id.item_iv).setTag(R.id.item_iv, item);
        helper.addOnClickListener(R.id.item_iv);
        ImageView imageView = helper.getView(R.id.item_iv);
        imageView.setAdjustViewBounds(false);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.height = (ScreenUtils.getAppScreenWidth() - ImageListView.dividerSize) / ImageListView.COUNT;
        imageView.setLayoutParams(params);
        String name = item.path.substring(item.path.lastIndexOf("/")+1);

        if(item.isDir){
            helper.setText(R.id.tv_info, "文件夹:"+name);
            imageView.setImageResource(R.drawable.icon_folder_imgs);
           /* ImageLoader.with(helper.itemView.getContext())
                    .res(R.drawable.ic_empty_page_2)
                    //.loading(R.drawable.iv_loading_trans)
                    .defaultPlaceHolder(true)
                    .scale(ScaleMode.CENTER_CROP)
                    .error(R.drawable.im_item_list_opt_error)
                    .into(helper.getView(R.id.item_iv));*/
            return;

        }
        helper.setText(R.id.tv_info, name);




        ImageLoader.with(helper.itemView.getContext())
                .load(item.path)
                //.loading(R.drawable.iv_loading_trans)
                .defaultPlaceHolder(true)
                .scale(ScaleMode.CENTER_CROP)
                .error(R.drawable.im_item_list_opt_error)
                .into(helper.getView(R.id.item_iv));
        if (item.width != 0) {
            String text = item.width + "x" + item.height + "," + MyUtil.formatFileSize(item.fileSize);
           /* if(item.oritation ==0){
                try {
                    ExifInterface exifInterface = new ExifInterface(item.path);
                    int attr =   exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,0);
                    if(attr == ExifInterface.ORIENTATION_ROTATE_90){
                        text = text + "\n90c";
                        item.oritation = 90;
                    }else   if(attr == ExifInterface.ORIENTATION_ROTATE_270){
                        text = text + "\n270c";
                        item.oritation = 270;
                    }else   if(attr == ExifInterface.ORIENTATION_ROTATE_180){
                        text = text + "\n180c";
                        item.oritation = 180;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }*/
            helper.setText(R.id.tv_info, text+"\n"+name);
        } else {
            int[] wh = MyUtil.getImageWidthHeight(item.path);
            item.width = wh[0];
            item.height = wh[1];
            item.fileSize = new File(item.path).length();

            String text = item.width + "x" + item.height + "," + MyUtil.formatFileSize(item.fileSize)
                    +"\n"+item.path.substring(item.path.lastIndexOf("/")+1);
            helper.setText(R.id.tv_info, text+"\n"+name);
           /* try {
                ExifInterface exifInterface = new ExifInterface(item.path);
                int attr =   exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,0);
                if(attr == ExifInterface.ORIENTATION_ROTATE_90){
                    text = text + "\n90c";
                    item.oritation = 90;
                }else   if(attr == ExifInterface.ORIENTATION_ROTATE_270){
                    text = text + "\n270c";
                    item.oritation = 270;
                }else   if(attr == ExifInterface.ORIENTATION_ROTATE_180){
                    text = text + "\n180c";
                    item.oritation = 180;
                }
                 helper.setText(R.id.tv_info, text);
            } catch (IOException e) {
                e.printStackTrace();
            }*/



            helper.setText(R.id.tv_info, item.width + "x" + item.height + "," + MyUtil.formatFileSize(item.fileSize));
        }




    }


    @Override
    public String getSectionTitle(int position) {
        return position + "";
    }
}
