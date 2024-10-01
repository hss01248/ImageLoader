package com.hss01248.imagelist.album;

import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.config.ScaleMode;
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
        helper.getView(R.id.item_iv).setTag(R.id.item_iv, item);
        helper.addOnClickListener(R.id.item_iv);
        ImageView imageView = helper.getView(R.id.item_iv);
        imageView.setAdjustViewBounds(false);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.height = (ScreenUtils.getAppScreenWidth() - ImageListView.dividerSize) / ImageListView.count();
        imageView.setLayoutParams(params);
        String name = item.path.substring(item.path.lastIndexOf("/")+1);

        if(item.isDir){
            helper.setText(R.id.tv_info, "文件夹 "+name);
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
           ThreadUtils.executeByIo(new ThreadUtils.Task<Object>() {
               @Override
               public Object doInBackground() throws Throwable {
                   int[] wh = MyUtil.getImageWidthHeight(item.path);//文件io
                   item.width = wh[0];
                   item.height = wh[1];
                   return null;
               }

               @Override
               public void onSuccess(Object result) {
                   helper.setText(R.id.tv_info, item.width + "x" + item.height + "," + MyUtil.formatFileSize(item.fileSize));
               }

               @Override
               public void onCancel() {

               }

               @Override
               public void onFail(Throwable t) {

               }
           });
           /* int[] wh = MyUtil.getImageWidthHeight(item.path);//文件io
            item.width = wh[0];
            item.height = wh[1];*/
            item.fileSize = new File(item.path).length();

           /* String text = item.width + "x" + item.height + "," + MyUtil.formatFileSize(item.fileSize)
                    +"\n"+item.path.substring(item.path.lastIndexOf("/")+1);
            helper.setText(R.id.tv_info, text+"\n"+name);*/
            helper.setText(R.id.tv_info, item.width + "x" + item.height + "," + MyUtil.formatFileSize(item.fileSize));
        }




    }


    @Override
    public String getSectionTitle(int position) {
        return position + "";
    }
}
