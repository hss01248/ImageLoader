package com.hss01248.imagelist.album;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.imagelist.NormalCallback;
import com.hss01248.imagelist.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * time:2019/11/30
 * author:hss
 * desription:
 */
public class ImageListView extends FrameLayout {

    private static final String TAG = "ImageListView";
    RecyclerView recyclerView;
    String dir;
    boolean isAll;
    FastScroller fastScroller;
    RecyclerView.Adapter adapter;

    public ImageListView( Context context) {
        this(context,null,0);
    }

    public ImageListView( Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ImageListView( Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View layout = View.inflate(context, R.layout.imagelistview,null);
        this.addView(layout,0);
         recyclerView = findViewById(R.id.list);
        fastScroller = (FastScroller) findViewById(R.id.fastscroll);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    /**
     *
     * @param urls 网络链接
     * @param downloadDir 图片转存一份到某个文件夹,可以为空.为空代表不转存
     * @param hideDir 是否要隐藏文件夹
     */
    public void showUrls(String pageTitle, final List<String> urls, @Nullable String downloadDir, boolean hideDir){
        downloadAndSave(pageTitle,urls,downloadDir,hideDir);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        ImgItemAdapter adapter = new ImgItemAdapter(R.layout.imglist_item_iv, urls);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        fastScroller.setRecyclerView(recyclerView);

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ImageMediaCenterUtil.showBigImag(getContext(),urls,position);
            }
        });
    }




    public void showImagesInDir(String dirPath){
        File dir = new File(dirPath);
        if(!dir.exists() || !dir.isDirectory()){
            Log.w(TAG,dirPath  + " is not exist or not a directory!");
            return;
        }
        String[] files = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        });
        if(files == null || files.length ==0){
            Log.w(TAG,dirPath  + " is not exist or not a directory!");
            return;
        }
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(files.length > 100 ? 3 : 2, StaggeredGridLayoutManager.VERTICAL));
        final List<String> paths = new ArrayList<>();
        for (String name : files) {
            paths.add(dirPath+"/"+name);
        }
        ImgItemAdapter imgItemAdapter = null;
        if(adapter !=null && adapter instanceof ImgItemAdapter){
             imgItemAdapter = (ImgItemAdapter) adapter;
            imgItemAdapter.replaceData(paths);
        }else {
             imgItemAdapter = new ImgItemAdapter(R.layout.imglist_item_iv, paths);
            adapter = imgItemAdapter;
            recyclerView.setAdapter(imgItemAdapter);
            imgItemAdapter.notifyDataSetChanged();
            fastScroller.setRecyclerView(recyclerView);
        }
        imgItemAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ImageMediaCenterUtil.showBigImag(getContext(),paths,position);
            }
        });




    }

    public void showImagesInAlbum(String albumName){

        ImageMediaCenterUtil.listImagesByAlbumName(getContext(), albumName, new NormalCallback<List<Image>>() {
            @Override
            public void onSuccess(List<Image> images, Object extra) {
                final List<String> paths = new ArrayList<>(images.size());
                for (Image image : images) {
                    paths.add(image.path);
                }
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(images.size() > 100 ? 3 : 2, StaggeredGridLayoutManager.VERTICAL));
                ImgItemAdapter imgItemAdapter = new ImgItemAdapter(R.layout.imglist_item_iv, paths);
                adapter = imgItemAdapter;
                recyclerView.setAdapter(imgItemAdapter);
                imgItemAdapter.notifyDataSetChanged();
                fastScroller.setRecyclerView(recyclerView);

                imgItemAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                    @Override
                    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                        ImageMediaCenterUtil.showBigImag(getContext(),paths,position);
                    }
                });
            }

            @Override
            public void onFail(Throwable e) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void showAllAlbums(){
        ImageMediaCenterUtil.getAlbums(getContext(), new NormalCallback<List<Album>>() {
            @Override
            public void onSuccess(final List<Album> albums, Object extra) {
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(albums.size() > 100 ? 3 : 2, StaggeredGridLayoutManager.VERTICAL));
                AlbumAdapter imgItemAdapter = new AlbumAdapter(R.layout.imglist_item_iv, albums);
                recyclerView.setAdapter(imgItemAdapter);
                imgItemAdapter.notifyDataSetChanged();
                fastScroller.setRecyclerView(recyclerView);


            }

            @Override
            public void onFail(Throwable e) {

            }

            @Override
            public void onCancel() {

            }
        });

    }


    private void downloadAndSave(final String title, List<String> urls, String downloadDir, boolean hideDir) {
        final File dir = new File (downloadDir);
        if(!dir.exists()){
            dir.mkdirs();
        }
        if(hideDir){
            File hidden = new File(dir,".nomedia");
            if(!hidden.exists()){
                try {
                    hidden.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            File hidden = new File(dir,".nomedia");
            if(hidden.exists()){
                hidden.delete();
            }
        }

        for (final String url : urls) {
            ImageLoader.getActualLoader().download(url, new FileGetter() {
                @Override
                public void onSuccess(File file, int width, int height) {
                    String name = title + "-"+ URLUtil.guessFileName(url,"","image/*");

                    copyFile(file, name, dir);
                }

                @Override
                public void onFail(Throwable e) {
                    e.printStackTrace();

                }
            });
        }

    }

    ExecutorService executors;
    private void copyFile(final File file, final String name, final File dir) {
        if(executors == null){
            executors = Executors.newFixedThreadPool(4);
        }
        executors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtils.copyFile(file, new File(dir, name));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
