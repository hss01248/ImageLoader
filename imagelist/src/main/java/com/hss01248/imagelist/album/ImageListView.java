package com.hss01248.imagelist.album;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.fondesa.recyclerviewdivider.DividerDecoration;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.gson.internal.$Gson$Preconditions;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.imagelist.NormalCallback;
import com.hss01248.imagelist.R;
import com.hss01248.imagelist.download.ImgDownloader;
import com.hss01248.ui.pop.list.PopList;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    BaseQuickAdapter adapter;
    RelativeLayout titleBar;
    TextView title;
    TextView tvRIght;

    public static int dividerSize = SizeUtils.dp2px(2);
    public static final int COUNT = 3;


    public ImageListView(Context context) {
        this(context, null, 0);
    }

    public ImageListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View layout = View.inflate(context, R.layout.imagelistview, null);
        this.addView(layout, 0);
        recyclerView = findViewById(R.id.list);
        fastScroller = (FastScroller) findViewById(R.id.fastscroll);
        titleBar = findViewById(R.id.titlebar);
        title = findViewById(R.id.tv_title);
        tvRIght = findViewById(R.id.tv_right);
        initTitlebar();

    }

    private void initTitlebar() {
        tvRIght.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final BaseQuickAdapter adapter = (BaseQuickAdapter) recyclerView.getAdapter();
                final List datas = adapter.getData();
                if (datas.get(0) instanceof Image) {
                    String[] strings = new String[]{"日期", "日期(倒序)", "文件名", "文件大小", "文件大小(倒序)"};

                    new AlertDialog.Builder(v.getContext())
                            .setItems(strings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, final int which) {
                                    dialog.dismiss();

                                    List<Image> datas2 = datas;
                                    Collections.sort(datas2, new Comparator<Image>() {
                                        @Override
                                        public int compare(Image o1, Image o2) {
                                            if (which == 0) {
                                                return (int) (o1.addDate - o2.addDate);
                                            } else if (which == 1) {
                                                return (int) (o2.addDate - o1.addDate);
                                            } else if (which == 2) {
                                                return o1.name.compareTo(o2.name);
                                            } else if (which == 3) {
                                                return (int) (o1.fileSize - o2.fileSize);
                                            } else if (which == 4) {
                                                return (int) (o2.fileSize - o1.fileSize);
                                            } else {
                                                return (int) (o2.addDate - o1.addDate);
                                            }

                                        }
                                    });
                                    adapter.replaceData(datas2);

                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create().show();


                }

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    /**
     * @param urls        网络链接
     * @param downloadDir 图片转存一份到某个文件夹,可以为空.为空代表不转存
     * @param hideDir     是否要隐藏文件夹
     */
    public void showUrls(String pageTitle, final List<String> urls, @Nullable String downloadDir, boolean hideDir) {
        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),COUNT));
        ImgItemAdapter adapter = new ImgItemAdapter(R.layout.imglist_item_iv, urls);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        setDivider(recyclerView);
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setVisibility(VISIBLE);
        titleBar.setVisibility(VISIBLE);
        title.setText(pageTitle);

        tvRIght.setVisibility(VISIBLE);

        tvRIght.setText("menu");
        tvRIght.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> menu = new ArrayList<>();
                menu.add("下载所有");
                menu.add("查看本地文件夹");
                PopList.showPop(getContext(), ViewGroup.LayoutParams.MATCH_PARENT, tvRIght, menu, new PopList.OnItemClickListener() {
                    @Override
                    public void onClick(int position, String str) {
                        if(position == 0){
                            downloadAndSave(pageTitle, urls, downloadDir, hideDir,null);
                        }else if(position == 1){
                           showImagesInDir(downloadDir);
                        }

                    }
                });
            }
        });


        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ImageMediaCenterUtil.showBigImag(getContext(), urls, position);
            }
        });
    }

    public void showUrlsFromMap(String pageTitle, Map<String,List<String>> titlesToImags, List<String> urls, @Nullable String downloadDir, boolean hideDir) {
        if(urls == null || urls.isEmpty()){
            urls = new ArrayList<>();
        }else {

        }
        Map<String,String> urlTitleMap = new HashMap<>();
        for (Map.Entry<String, List<String>> stringListEntry : titlesToImags.entrySet()) {
            //urls.addAll(stringListEntry.getValue());
            for (String s : stringListEntry.getValue()) {
                urlTitleMap.put(s,stringListEntry.getKey());
            }
        }

        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),COUNT));
        ImgItemAdapter adapter = new ImgItemAdapter(R.layout.imglist_item_iv, urls);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        setDivider(recyclerView);
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setVisibility(VISIBLE);
        titleBar.setVisibility(VISIBLE);
        title.setText(pageTitle);

        tvRIght.setVisibility(VISIBLE);

        tvRIght.setText("menu");
        List<String> finalUrls1 = urls;
        tvRIght.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> menu = new ArrayList<>();
                menu.add("下载所有");
                menu.add("查看本地文件夹");
                PopList.showPop(getContext(), ViewGroup.LayoutParams.MATCH_PARENT, tvRIght, menu, new PopList.OnItemClickListener() {
                    @Override
                    public void onClick(int position, String str) {
                        if(position == 0){
                            downloadAndSave(pageTitle, finalUrls1, downloadDir, hideDir, new ImgDownloader.IFileNamePrefix() {
                                @Override
                                public String getFileNamePreffix(String url) {
                                    return urlTitleMap.get(url);
                                }
                            });

                        }else if(position == 1){
                            showImagesInDir(downloadDir);
                        }

                    }
                });
            }
        });


        List<String> finalUrls = urls;
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ImageMediaCenterUtil.showBigImag(getContext(), finalUrls, position);
            }
        });
    }


    public void showImagesInDir(final String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            Log.w(TAG, dirPath + " is not exist or not a directory!");
            return;
        }
        /*ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("展示文件夹");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();*/
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            //dialog.dismiss();
            ToastUtils.showShort("folder is empty");
            Log.w(TAG, dirPath + " is not exist or not a directory!");
            return;
        }
        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),COUNT));

        final List<Image> images = new ArrayList<>();
        for (File file : files) {
            Image image = new Image(0, file.getName(), dirPath + "/" + file.getName(), false);
            image.isDir = file.isDirectory();
            images.add(image);
        }
        Collections.sort(images, new Comparator<Image>() {
            @Override
            public int compare(Image image, Image t1) {
                if(image.isDir && !t1.isDir){
                    return -1;
                }
                if(!image.isDir && t1.isDir){
                    return 1;
                }
                return image.name.compareTo(t1.name);
            }
        });

        final AlbumImgAdapter imgItemAdapter = new AlbumImgAdapter(R.layout.imglist_item_iv, images);
        adapter = imgItemAdapter;
        recyclerView.setAdapter(imgItemAdapter);
        setDivider(recyclerView);
        imgItemAdapter.notifyDataSetChanged();
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setVisibility(VISIBLE);
        titleBar.setVisibility(VISIBLE);
        title.setText(dirPath);

        imgItemAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if(images.get(position).isDir){
                    ImageListView listView = new ImageListView(getContext());
                    listView.showImagesInDir(images.get(position).path);
                    ImageMediaCenterUtil.showViewAsDialog(listView);

                }else {
                    final List<String> paths = new ArrayList<>();
                    List<Image> images2 = imgItemAdapter.getData();
                    for (Image image : images2) {
                        paths.add(image.path);
                    }
                    ImageMediaCenterUtil.showBigImag(getContext(), paths, position);
                }

            }
        });
       // dialog.dismiss();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Image image : images) {
                    image.initFileSize();
                }
            }
        }).start();


    }

    private void setDivider(RecyclerView recyclerView) {

        recyclerView.addItemDecoration(DividerDecoration.builder(getContext()).color(Color.WHITE).size(dividerSize).build());
        //recyclerView.addItemDecoration(StaggeredDividerDecoration.builder(getContext()).color(Color.WHITE).size(10).build());

    }

    public void showImagesInAlbum(Album album) {
        titleBar.setVisibility(VISIBLE);
        List<Image> cachedImages = new ArrayList<>();
        ImageMediaCenterUtil.listImagesByAlbumName(getContext(), album.id, new NormalCallback<List<Image>>() {
            @Override
            public void onSuccess(final List<Image> images, Object extra) {
                LogUtils.d("onsuccess:"+images.size());
                if(recyclerView.getAdapter() == null){
                    initRecyclerviewByLocalImages(images);
                }else {
                    adapter.getData().clear();
                    adapter.getData().addAll(images);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFirst50Success(List<Image> images, Object extra) {
                LogUtils.d("onFirst50Success:"+images.size());
                cachedImages.clear();
                cachedImages.addAll(images);
                if(recyclerView.getAdapter() == null){
                    initRecyclerviewByLocalImages(cachedImages);
                }


            }

            @Override
            public void onFail(Throwable e) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void initRecyclerviewByLocalImages(List<Image> cachedImages) {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),COUNT));
        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        final AlbumImgAdapter imgItemAdapter = new AlbumImgAdapter(R.layout.imglist_item_iv, cachedImages);
        adapter = imgItemAdapter;
        recyclerView.setAdapter(imgItemAdapter);
        setDivider(recyclerView);
        imgItemAdapter.notifyDataSetChanged();
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setVisibility(VISIBLE);

        imgItemAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                final List<String> paths = new ArrayList<>();
                List<Image> images2 = imgItemAdapter.getData();
                for (Image image : images2) {
                    paths.add(image.path);
                }
                ImageMediaCenterUtil.showBigImag(getContext(), paths, position);
            }
        });
    }

    public void showAllAlbums() {
        ImageMediaCenterUtil.getAlbums(getContext(), new NormalCallback<List<Album>>() {
            @Override
            public void onSuccess(final List<Album> albums, Object extra) {
                //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(),COUNT));
                AlbumAdapter imgItemAdapter = new AlbumAdapter(R.layout.imglist_item_iv, albums);
                recyclerView.setAdapter(imgItemAdapter);
                setDivider(recyclerView);
                imgItemAdapter.notifyDataSetChanged();
                fastScroller.setRecyclerView(recyclerView);
                fastScroller.setVisibility(VISIBLE);


            }

            @Override
            public void onFail(Throwable e) {

            }

            @Override
            public void onCancel() {

            }
        });

    }


    private void downloadAndSave(final String title, List<String> urls, String downloadDir, boolean hideDir, ImgDownloader.IFileNamePrefix fileNamePrefix) {
        if (TextUtils.isEmpty(downloadDir)) {
            return;
        }
        final File dir = new File(downloadDir);

        if (!dir.exists()) {
            boolean succes = dir.mkdirs();
            if (!succes) {
                Log.w("Imagelistview", "mkdir failed:" + downloadDir);
                //return;
            }
        } else {
            if (!dir.isDirectory()) {
                Log.w("Imagelistview", "not a directory:" + downloadDir);
               // return;
            }
        }

        if (hideDir) {
            File hidden = new File(dir, ".nomedia");
            if (!hidden.exists()) {
                try {
                    hidden.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File hidden = new File(dir, ".nomedia");
            if (hidden.exists()) {
                hidden.delete();
            }
        }

        new ImgDownloader().download(getContext(),urls,dir,hideDir,title,fileNamePrefix);
       /* for (final String url : urls) {
            ImageLoader.getActualLoader().download(url, new FileGetter() {
                @Override
                public void onSuccess(File file, int width, int height) {
                    String name = title + "-" + URLUtil.guessFileName(url, "", "image/*");

                    copyFile(file, name, dir);
                }

                @Override
                public void onFail(Throwable e) {
                    e.printStackTrace();

                }
            });
        }*/

    }

    ExecutorService executors;

    private void copyFile(final File file, final String name, final File dir) {
        if (executors == null) {
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
