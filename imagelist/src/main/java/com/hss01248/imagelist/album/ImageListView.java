package com.hss01248.imagelist.album;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.fondesa.recyclerviewdivider.DividerDecoration;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.hss.downloader.DownloadUrls;
import com.hss.downloader.MyDownloader;

import com.hss01248.bigimageviewpager.LargeImageViewer;
import com.hss01248.imagelist.NormalCallback;
import com.hss01248.imagelist.R;

import com.hss01248.img.compressor.ImageDirCompressor;
import com.hss01248.img.compressor.UiForDirCompress;
import com.hss01248.iwidget.singlechoose.ISingleChooseItem;
import com.hss01248.permission.MyPermissions;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static int dividerSize = SizeUtils.dp2px(1);
    public static final int count(){
        if(ScreenUtils.isLandscape()){
            if(DeviceUtils.isTablet()){
                return 9;
            }else {
                return 6;
            }
        }else {
            if(DeviceUtils.isTablet()){
                return 5;
            }else {
                return 3;
            }
        }
    }


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
                if (!TextUtils.isEmpty(dir)) {

                }
                String[] strings0 = new String[]{"排序", "压缩", "过滤", "删除当前所有文件"};

                new AlertDialog.Builder(v.getContext())
                        .setItems(strings0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                dialog.dismiss();
                                if (which == 0) {
                                    reOrder(v);
                                } else if (which == 1) {
                                    compressDir();
                                } else if (which == 2) {
                                    filterDir(dir);
                                } else if (which == 3) {
                                    deleteAll();
                                }

                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();


            }
        });
    }

    private void deleteAll() {
        List data = adapter.getData();
        List<File> files = new ArrayList<>(data.size());
        long length = 0;
        for (Object datum : data) {
            if (datum instanceof Image) {
                Image image = (Image) datum;
                if (!image.isDir) {
                    File file = new File(image.path);
                    files.add(file);
                    length += file.length();
                }
            }
        }
        String str = "当前有" + files.size() + "个文件,大小为:" + ConvertUtils.byte2FitMemorySize(length, 1) + "\n是否删除当前所有文件?";
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("批量删除文件")
                .setMessage(str)
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ThreadUtils.executeByIo(new ThreadUtils.Task<Object>() {
                            @Override
                            public Object doInBackground() throws Throwable {
                                for (File file : files) {
                                    file.delete();
                                }
                                return null;
                            }

                            @Override
                            public void onSuccess(Object result) {
                                ToastUtils.showLong("删除完成");

                            }

                            @Override
                            public void onCancel() {

                            }

                            @Override
                            public void onFail(Throwable t) {
                                ToastUtils.showLong("删除失败:" + t.getMessage());

                            }
                        });
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    private void filterDir(String dir) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        EditText editText = new EditText(getContext());
        //editText.setPadding(SizeUtils.dp2px(20),0,SizeUtils.dp2px(20),0);

        builder.setTitle("根据文件名过滤")
                .setMessage("只保留文件名里含输入内容的文件\n为空则显示当前文件夹全部文件")
                .setView(editText)
                .setPositiveButton("过滤", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                            ToastUtils.showLong("输入为空,显示全部文件");
                            doFilter("");
                        } else {
                            String text = editText.getText().toString().trim();
                            doFilter(text);

                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
            }
        });
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        ViewGroup.LayoutParams layoutParams = editText.getLayoutParams();
        if (layoutParams instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams params = (LayoutParams) layoutParams;
            params.leftMargin = SizeUtils.dp2px(22);
            params.rightMargin = SizeUtils.dp2px(22);
            editText.setLayoutParams(params);
        }


    }

    private void doFilter(String text) {
        ThreadUtils.executeByIo(new ThreadUtils.Task<List<Image>>() {
            @Override
            public List<Image> doInBackground() throws Throwable {
                File[] files = new File(dir).listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if (TextUtils.isEmpty(text)) {
                            return true;
                        }
                        return name.contains(text);
                    }
                });
                final List<Image> images = new ArrayList<>();
                for (File file : files) {
                    Image image = new Image(0, file.getName(), dir + "/" + file.getName(), false);
                    image.isDir = file.isDirectory();
                    images.add(image);
                }
                Collections.sort(images, new Comparator<Image>() {
                    @Override
                    public int compare(Image image, Image t1) {
                        if (image.isDir && !t1.isDir) {
                            return -1;
                        }
                        if (!image.isDir && t1.isDir) {
                            return 1;
                        }
                        return image.name.toLowerCase().compareTo(t1.name.toLowerCase());
                    }
                });
                return images;
            }

            @Override
            public void onSuccess(List<Image> result) {
                adapter.setNewData(result);

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                t.printStackTrace();

            }
        });
    }

    private void compressDir() {
        ImageDirCompressor.compressDir(dir, new UiForDirCompress() {
            @Override
            public void showDirImags(String dir0) {
                ImageMediaCenterUtil.showImagesInDir(getContext(), dir0);

            }
        });

    }

    private void reOrder(View v) {
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
                                        if (o1.isDir && !o2.isDir) {
                                            return -1;
                                        }
                                        if (!o2.isDir && o1.isDir) {
                                            return 1;
                                        }
                                        return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    /**
     * @param urls        网络链接
     * @param downloadDir 图片转存一份到某个文件夹,可以为空.为空代表不转存
     * @param hideDir     是否要隐藏文件夹
     */
    public void showUrls(String pageTitle, final List<String> urls, @Nullable String downloadDir, boolean hideDir, boolean downloadImmediately) {
        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), count()));
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
        downloadDir = ImgDirUtil.dealFolderCount(new File(downloadDir), hideDir).getAbsolutePath();
        String finalDownloadDir = downloadDir;
        tvRIght.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                List<ISingleChooseItem<String>> items = new ArrayList<>();
                items.add(new ISingleChooseItem<String>() {
                    @Override
                    public String text() {
                        return "下载所有";
                    }

                    @Override
                    public void onItemClicked(int position, String bean) {
                        downloadAndSave(pageTitle, urls, finalDownloadDir, hideDir, null);
                    }
                });
                items.add(new ISingleChooseItem<String>() {
                    @Override
                    public String text() {
                        return "查看当前下载的文件夹";
                    }

                    @Override
                    public void onItemClicked(int position, String bean) {
                        showImagesInDir(finalDownloadDir);
                    }
                });

                ISingleChooseItem.showAsMenu(view, items, "");
            }
        });
        if (downloadImmediately) {
            downloadAndSave(pageTitle, urls, downloadDir, hideDir, null);
        }


        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ImageMediaCenterUtil.showBigImag(getContext(), urls, position);
            }
        });
    }

    public void showUrlsFromMap(String pageTitle, Map<String, List<String>> titlesToImags, List<String> urls, @Nullable String downloadDir, boolean hideDir, boolean downloadImmediately) {
        if (urls == null || urls.isEmpty()) {
            urls = new ArrayList<>();
        } else {

        }
        Map<String, String> urlTitleMap = new HashMap<>();
        for (Map.Entry<String, List<String>> stringListEntry : titlesToImags.entrySet()) {
            //urls.addAll(stringListEntry.getValue());
            for (String s : stringListEntry.getValue()) {
                urlTitleMap.put(s, stringListEntry.getKey());
            }
        }

        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), count()));
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
        downloadDir = ImgDirUtil.dealFolderCount(new File(downloadDir), hideDir).getAbsolutePath();
        String finalDownloadDir = downloadDir;
        tvRIght.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                List<ISingleChooseItem<String>> items = new ArrayList<>();
                items.add(new ISingleChooseItem<String>() {
                    @Override
                    public String text() {
                        return "下载所有";
                    }

                    @Override
                    public void onItemClicked(int position, String bean) {
                        downloadAndSave(pageTitle, finalUrls1, finalDownloadDir, hideDir, new IFileNamePrefix() {
                            @Override
                            public String getFileNamePreffix(String url) {
                                return urlTitleMap.get(url);
                            }
                        });
                    }
                });
                items.add(new ISingleChooseItem<String>() {
                    @Override
                    public String text() {
                        return "查看当前下载的文件夹";
                    }

                    @Override
                    public void onItemClicked(int position, String bean) {
                        showImagesInDir(finalDownloadDir);
                    }
                });

                ISingleChooseItem.showAsMenu(view, items, "");
            }
        });

        if (downloadImmediately) {
            downloadAndSave(pageTitle, finalUrls1, downloadDir, hideDir, new IFileNamePrefix() {
                @Override
                public String getFileNamePreffix(String url) {
                    return urlTitleMap.get(url);
                }
            });
        }


        List<String> finalUrls = urls;
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ImageMediaCenterUtil.showBigImag(getContext(), finalUrls, position);
            }
        });
    }


    public void showImagesInDir(final String dirPath) {
        dir = dirPath;
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            Log.w(TAG, dirPath + " is not exist or not a directory!");
            return;
        }
        /*ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("展示文件夹");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();*/
        File[] files = dir.listFiles();//todo 主线程io
        if (files == null || files.length == 0) {
            //dialog.dismiss();
            ToastUtils.showShort("folder is empty");
            Log.w(TAG, dirPath + " is not exist or not a directory!");
            return;
        }
        //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), count()));

        final List<Image> images = new ArrayList<>();
        for (File file : files) {
            Image image = new Image(0, file.getName(), dirPath + "/" + file.getName(), false);
            image.isDir = file.isDirectory();
            images.add(image);
        }
        Collections.sort(images, new Comparator<Image>() {
            @Override
            public int compare(Image image, Image t1) {
                if (image.isDir && !t1.isDir) {
                    return -1;
                }
                if (!image.isDir && t1.isDir) {
                    return 1;
                }
                return image.name.toLowerCase().compareTo(t1.name.toLowerCase());
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
                if (images.get(position).isDir) {

                    // ImageMediaCenterUtil.showViewAsDialog(listView);
                    ImageMediaCenterUtil.showViewAsActivityOrDialog(view.getContext(), false, new IViewInit() {
                        @Override
                        public View init(Activity activity) {
                            ImageListView listView = new ImageListView(activity);
                            listView.showImagesInDir(images.get(position).path);
                            return listView;
                        }
                    });

                } else {
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

        recyclerView.addItemDecoration(DividerDecoration.builder(getContext()).color(Color.BLACK).size(dividerSize).build());
        //recyclerView.addItemDecoration(StaggeredDividerDecoration.builder(getContext()).color(Color.WHITE).size(10).build());

    }

    public void showImagesInAlbum(Album album) {
        titleBar.setVisibility(VISIBLE);
        List<Image> cachedImages = new ArrayList<>();
        ImageMediaCenterUtil.listImagesByAlbumName(getContext(), album.id, new NormalCallback<List<Image>>() {
            @Override
            public void onSuccess(final List<Image> images, Object extra) {
                LogUtils.d("onsuccess:" + images.size());
                if (recyclerView.getAdapter() == null) {
                    initRecyclerviewByLocalImages(images);
                } else {
                    adapter.getData().clear();
                    adapter.getData().addAll(images);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFirst50Success(List<Image> images, Object extra) {
                LogUtils.d("onFirst50Success:" + images.size());
                cachedImages.clear();
                cachedImages.addAll(images);
                if (recyclerView.getAdapter() == null) {
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
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), count()));
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            MyPermissions.request(new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(@NonNull List<String> granted) {
                    loadAlbumsAfterPermission();
                }

                @Override
                public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                    ToastUtils.showShort("no permission");

                }
            }, Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO);
        }else {
            MyPermissions.request(new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(@NonNull List<String> granted) {
                    loadAlbumsAfterPermission();
                }

                @Override
                public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                    ToastUtils.showShort("no permission");
                }
            }, Manifest.permission.READ_EXTERNAL_STORAGE);
        }


    }

    private void loadAlbumsAfterPermission() {
        ImageMediaCenterUtil.getAlbums(getContext(), new NormalCallback<List<Album>>() {
            @Override
            public void onSuccess(final List<Album> albums, Object extra) {
                if(albums == null || albums.isEmpty()){
                    ToastUtils.showShort("empty");
                    return;
                }
                //recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), count()));
                AlbumAdapter imgItemAdapter = new AlbumAdapter(R.layout.imglist_item_iv, albums);
                recyclerView.setAdapter(imgItemAdapter);
                setDivider(recyclerView);
                imgItemAdapter.notifyDataSetChanged();
                fastScroller.setRecyclerView(recyclerView);
                fastScroller.setVisibility(VISIBLE);


            }

            @Override
            public void onFail(Throwable e) {
                ToastUtils.showShort(e.getMessage());

            }

            @Override
            public void onCancel() {

            }
        });
    }

    public interface IFileNamePrefix {
        String getFileNamePreffix(String url);
    }


    private void downloadAndSave(final String title, List<String> urls, String downloadDir, boolean hideFolder, IFileNamePrefix fileNamePrefix) {
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (hideFolder) {
            File hidden = new File(dir, ".nomedia");
            if (!hidden.exists()) {
                try {
                    hidden.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        List<DownloadUrls> list = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            String pre = title;
            if (fileNamePrefix != null) {
                pre = fileNamePrefix.getFileNamePreffix(url);
            }

            //特殊字符的解决方案: https://www.cxybb.com/article/weixin_42834380/103633387
            if (!TextUtils.isEmpty(pre)) {
                if (pre.length() > 50) {
                    pre = pre.substring(0, 50);
                }
            }
            String name = pre + "-" + String.format("%04d", i) + "-" + URLUtil.guessFileName(url, "", "image/*");
            //处理文件长度太长的情况
            //Linux文件名的长度限制是255个字符
            //windows下完全限定文件名必须少于260个字符，目录名必须小于248个字符。

            DownloadUrls info = new DownloadUrls();
            info.url = LargeImageViewer.getBigImageUrl(url);
            info.name = name;

            info.dir = downloadDir;
            list.add(info);
        }

        MyDownloader.download(list);


        //new ImgDownloader().download(getContext(),urls,new File(downloadDir),hideDir,title,fileNamePrefix);
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
