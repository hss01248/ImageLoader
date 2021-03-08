package com.hss01248.imagelist.album;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.Collections;
import java.util.Comparator;
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
    RelativeLayout titleBar;
    TextView title;
    TextView tvRIght;


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
        title = findViewById(R.id.title);
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
        downloadAndSave(pageTitle, urls, downloadDir, hideDir);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        ImgItemAdapter adapter = new ImgItemAdapter(R.layout.imglist_item_iv, urls);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setVisibility(VISIBLE);

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ImageMediaCenterUtil.showBigImag(getContext(), urls, position);
            }
        });
    }


    public void showImagesInDir(final String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            Log.w(TAG, dirPath + " is not exist or not a directory!");
            return;
        }
        String[] files = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        });
        if (files == null || files.length == 0) {
            Log.w(TAG, dirPath + " is not exist or not a directory!");
            return;
        }
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(files.length > 100 ? 3 : 2, StaggeredGridLayoutManager.VERTICAL));

        final List<Image> images = new ArrayList<>();
        for (String name : files) {

            Image image = new Image(0, name, dirPath + "/" + name, false);
            images.add(image);
        }

        final AlbumImgAdapter imgItemAdapter = new AlbumImgAdapter(R.layout.imglist_item_iv, images);
        adapter = imgItemAdapter;
        recyclerView.setAdapter(imgItemAdapter);
        imgItemAdapter.notifyDataSetChanged();
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setVisibility(VISIBLE);
        titleBar.setVisibility(VISIBLE);

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Image image : images) {
                    image.initFileSize();
                }
            }
        }).start();


    }

    public void showImagesInAlbum(Album album) {
        titleBar.setVisibility(VISIBLE);
        ImageMediaCenterUtil.listImagesByAlbumName(getContext(), album.id, new NormalCallback<List<Image>>() {
            @Override
            public void onSuccess(final List<Image> images, Object extra) {
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(images.size() > 100 ? 3 : 2, StaggeredGridLayoutManager.VERTICAL));
                final AlbumImgAdapter imgItemAdapter = new AlbumImgAdapter(R.layout.imglist_item_iv, images);
                adapter = imgItemAdapter;
                recyclerView.setAdapter(imgItemAdapter);
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

            @Override
            public void onFail(Throwable e) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void showAllAlbums() {
        ImageMediaCenterUtil.getAlbums(getContext(), new NormalCallback<List<Album>>() {
            @Override
            public void onSuccess(final List<Album> albums, Object extra) {
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(albums.size() > 100 ? 3 : 2, StaggeredGridLayoutManager.VERTICAL));
                AlbumAdapter imgItemAdapter = new AlbumAdapter(R.layout.imglist_item_iv, albums);
                recyclerView.setAdapter(imgItemAdapter);
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


    private void downloadAndSave(final String title, List<String> urls, String downloadDir, boolean hideDir) {
        if (TextUtils.isEmpty(downloadDir)) {
            return;
        }
        final File dir = new File(downloadDir);

        if (!dir.exists()) {
            boolean succes = dir.mkdirs();
            if (!succes) {
                Log.w("Imagelistview", "mkdir failed:" + downloadDir);
                return;
            }
        } else {
            if (!dir.isDirectory()) {
                Log.w("Imagelistview", "not a directory:" + downloadDir);
                return;
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

        for (final String url : urls) {
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
        }

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
