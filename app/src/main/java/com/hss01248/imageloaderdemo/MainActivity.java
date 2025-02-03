package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.gson.Gson;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss.downloader.MyDownloader;
import com.hss.utils.enhance.api.MyCommonCallback;
import com.hss01248.basewebview.BaseWebviewActivity;
import com.hss01248.bigimageviewpager.LargeImageViewer;
import com.hss01248.bigimageviewpager.MyLargeImageView;
import com.hss01248.fileoperation.FileDeleteUtil;
import com.hss01248.fullscreendialog.FullScreenDialogUtil;
import com.hss01248.glide.aop.file.AddByteUtil;
import com.hss01248.glide.aop.file.DirOperationUtil;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.dataforphotoselet.ImgDataSeletor;
import com.hss01248.imagelist.album.IViewInit;
import com.hss01248.imagelist.album.ImageListView;
import com.hss01248.imagelist.album.ImageMediaCenterUtil;
import com.hss01248.imageloaderdemo.download.DownloadDemoActivity;
import com.hss01248.img.compressor.ImageCompressor;
import com.hss01248.img.compressor.ImageDirCompressor;
import com.hss01248.img.compressor.UiForDirCompress;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.media.pick.MediaPickUtil;
import com.hss01248.motion_photos.MotionPhotoUtil;
import com.hss01248.ui.pop.list.PopList;
import com.hss01248.viewholder_media.FileTreeViewHolder;
import com.hss01248.webviewspider.SpiderWebviewActivity;

import org.devio.takephoto.wrap.TakeOnePhotoListener;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.iv_url)
    ImageView ivUrl;
    @BindView(R.id.iv_file)
    ImageView ivFile;
    @BindView(R.id.iv_res)
    ImageView ivRes;
    @BindView(R.id.iv_url_blur)
    ImageView ivUrlBlur;

    @BindView(R.id.btn_bigpic)
    Button btnBigpic;
    @BindView(R.id.btn_bigpic_viewpager)
    Button btnBigpicViewpager;
    @BindView(R.id.activity_main)
    ScrollView activityMain;

    @BindView(R.id.btn_album)
    Button btnAlbum;
    @BindView(R.id.btn_dir)
    Button btnDir;

    SubsamplingScaleImageView subsamplingScaleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // GlideFaceDetector.initialize(this);
        subsamplingScaleImageView = findViewById(R.id.subsampling_iv);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                show();

                Debug.startMethodTracing("sp-read");
                SharedPreferences sharedPreferences = getSharedPreferences("ky", Context.MODE_PRIVATE);
                sharedPreferences.getString("shss", "ddddd");
                Debug.stopMethodTracing();
            }
        }, 5000);

        /*Debug.startMethodTracing("sp-write");
        SharedPreferences sharedPreferences = getSharedPreferences("ky", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("shss","1233444hhhhh").commit();
        //sharedPreferences.getString("shss","ddddd");
        Debug.stopMethodTracing();*/

        XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        Toast.makeText(MainActivity.this, all+"-These permissions are : "+ Arrays.toString(permissions.toArray()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void show() {

        List<String> datas = new ArrayList<>();


                //.widthHeight(250, 150)
        ;
                //.asCircle(R.color.colorAccent).into(ivUrl);
        //.blur(40)
                /*.asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {

                        ivUrl.setImageBitmap(bitmap);
                        Log.e("ee","ivUrl  bitmap.config:" + bitmap.getConfig());
                        *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ivUrl.setBackground(new BitmapDrawable(bitmap));
                        }*//*
                    }

                    @Override
                    public void onFail(Throwable e) {

                    }
                });*/
        // .into(ivUrl);


        ImageLoader.with(this)
                .placeHolder(R.mipmap.ic_launcher, false)
                .res(R.drawable.thegif)
                //.url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                .widthHeight(100, 80)
                .rectRoundCorner(5, R.color.colorPrimary);
        // .blur(5)
                /*.asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        Log.e("bitmap", bitmap.getWidth() + "---height:" + bitmap.getHeight() + "--" + bitmap.toString());

                        ivRes.setImageBitmap(bitmap);
                        Log.e("dd","ivRes  bitmap.config:" + bitmap.getConfig());
                    }

                    @Override
                    public void onFail(Throwable e) {
                        Log.e("bitmap", "fail");

                    }
                });*/
        //.into(ivRes);

        ImageLoader.with(this)
                .widthHeight(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                .rectRoundCorner(5, R.color.t_white);
        //.into(ivFile);

        ImageLoader.with(this)
                .cropFace()
                .widthHeight(100, 100)
                .asCircle(R.color.colorAccent)
                .blur(10)
                .url("http://img3.ynet.com/2018/03/22/071135542b5deabc409e36af01290c89_600x-_90.jpg");
        //.into(ivUrlBlur);


    }

    @OnClick({R.id.btn_bigpic, R.id.btn_bigpic_viewpager, R.id.btn_recycle, R.id.btn_fresco, R.id.btn_scale
            , R.id.btn_album, R.id.btn_dir})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_album: {
                /*oid.providers.media.MediaProvider uri content://media/external/images/media from
                pid=31500, uid=10576 requires android.permission.READ_EXTERNAL_STORAGE, or grantUriPermission()*/


                ImageMediaCenterUtil.showAlbums();


            }

            break;
            case R.id.btn_dir: {


                ImageMediaCenterUtil.showViewAsActivity(view.getContext(), new IViewInit() {
                    @Override
                    public View init(Activity activity) {
                        ImageListView view1 = new ImageListView(activity);
                       // ImageMediaCenterUtil.showViewAsDialog(view1);
                        view1.showImagesInDir(Environment.getExternalStorageDirectory().getAbsolutePath());
                        return view1;
                    }
                });
            }

            break;
            case R.id.btn_bigpic_viewpager: {
                Intent intent = new Intent(this, ViewpagerActy.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_recycle: {
                Intent intent = new Intent(this, RecycleViewActy.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_scale:
                Intent intent6 = new Intent(this, ScaleTypeActy.class);
                startActivity(intent6);

                break;
            default:
                break;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        //ImageMemoryHookManager.show(this);



    }



    public void loadUrl(View view) {
        BaseWebviewActivity.start(this,"https://www.hss01248.tech/mapsdemo2022.html");
       /* ImageLoader.with(this)
                .load("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
                // .url("http://img.yxbao.com/news/image/201703/13/7bda462477.gif")
                // .res(R.drawable.thegif)
                //.placeHolder(R.mipmap.ic_launcher, false)
                .loadingDefault()
                //.widthHeight(250, 150)
                //.asCircle(R.color.colorAccent)
                .into(ivUrl);*/
    }

    public void loadFile(View view) {
        ImageCompressor.compressToWebp = false;
        ImgDataSeletor.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
            @Override
            public void onSuccess(String path) {

                if(new File(path).isDirectory()){
                    ImageDirCompressor.compressDir(path,new UiForDirCompress(){

                        @Override
                        public void showDirImags(String dir) {
                            ImageMediaCenterUtil.showImagesInDir(view.getContext(),dir);
                        }
                    });

                    return;
                }
                ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<File>() {
                    @Override
                    public File doInBackground() throws Throwable {
                        return ImageCompressor.compress(path,false,false);
                    }

                    @Override
                    public void onSuccess(File file) {
                        LogUtils.i("image compress result: "+file.getAbsolutePath());
                        Glide.with(MainActivity.this)
                                .asBitmap()
                                .load(file)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        LogUtils.d("bitmap size:"+resource.getWidth()+"x"+resource.getHeight());
                                        ivUrl.setImageBitmap(resource);
                                    }
                                });
                        ivUrl.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<String> paths = new ArrayList<>();
                                paths.add(path);
                                paths.add(file.getAbsolutePath());
                                ImageMediaCenterUtil.showBigImag(MainActivity.this,paths,0);
                            }
                        });
                    }
                });

                //TurboCompressor.compressOriginal(path,70);
                //File file = ImageCompressor.compress(path,false,false);

               // File endecrypt = XorUtil.endecrypt(798, file, false);
               // LogUtils.i("out put file: "+endecrypt.getAbsolutePath());
                ///storage/emulated/0/images/enx-cp-20230529_200844.jpg  花费15s.



            }

            @Override
            public void onFail(String path, String msg) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void goWebSpider(View view) {
        List<String> menus = SpiderWebviewActivity.getSpiders();
        menus.add("浏览全部下载列表");
        menus.add("浏览下载列表");
        menus.add("修复升级前的数据");
        menus.add("继续下载未完成的图片");

        PopList.showPop(this, -1, view, menus, new PopList.OnItemClickListener() {
            @Override
            public void onClick(int position, String str) {
                if(position == menus.size()-1){
                    MyDownloader.continueDownload();
                    //ImgDownloader.downladUrlsInDB(MainActivity.this,new File(SpiderWebviewActivity.getSaveDir("继续下载","")));
                }else if(position ==  menus.size()-4) {
                    MyDownloader.showWholeDownloadPage();
                }else if(position ==  menus.size()-3) {
                    MyDownloader.showDownloadPage();
                }else if(position == menus.size()-2) {
                    MyDownloader.fixDbWhenUpdate();
                }else {
                    SpiderWebviewActivity.start(MainActivity.this,str);
                }

            }
        });


    }

    public void copyDB(View view) {
       /*File db =  getDatabasePath("imgdownload.db");
       File file2 = new File(Environment.getExternalStorageDirectory(),"/.yuv/databases/imgdownload.db");
        FileUtils.copy(db,file2);
        File db2 =  getDatabasePath("imgdownload.db-journal");
        File file3 = new File(Environment.getExternalStorageDirectory(),"/.yuv/databases/imgdownload.db-journal");
        FileUtils.copy(db2,file3);



        if(file2.exists() && file2.length() > 10){
            ToastUtils.showShort("copy db success!!");
        }else {
            ToastUtils.showShort("copy db fail!!");
        }*/
    }

    public void deleteFile(View view) {
        ImgDataSeletor.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
            @Override
            public void onSuccess(String path) {
               /* File file = new File(path);
                boolean delete = file.delete();
                ToastUtils.showLong("是否删除成功:"+ delete);*/
                //即使是true,也会被小米系统拦截
                FileDeleteUtil.deleteImage(path,true, new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Boolean aBoolean) {
                        ToastUtils.showLong("是否删除成功:"+ aBoolean);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

            }

            @Override
            public void onFail(String path, String msg) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void pickImage360(View view) {
        ImgDataSeletor.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
            @Override
            public void onSuccess(String path) {
               /* File file = new File(path);
                boolean delete = file.delete();
                ToastUtils.showLong("是否删除成功:"+ delete);*/
                //即使是true,也会被小米系统拦截
                showInfo(path);

                //FullScreenDialog dialog = new FullScreenDialog(MainActivity.this);

                //dialog.setContentView();

            }

            @Override
            public void onFail(String path, String msg) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    //当所述待选择图片的宽度大于或等于1000像素点，且所述待选择图片的宽高比大于或等于2:1，且所述待选择图片的宽高比小于4:1，则判断为所述待选择图片为360度全景图片。
    private void showInfo(String path) {
        String exifStr = ExifUtil.getExifStr(path);
        Dialog dialog = new Dialog(MainActivity.this);

        Map<String, String> map = ExifUtil.readExif(path);
        //dialog.setContentView();
        //LogUtils.iTag("exif",map.get("Xmp"));
        LogUtils.iTag("isPanoramaImage",isPanoramaImage(path),path);
        LogUtils.json(new Gson().newBuilder().setPrettyPrinting().create().toJson(map));


        MyLargeImageView largeImageView = new MyLargeImageView(MainActivity.this);


        dialog.setContentView(largeImageView);
        dialog.show();
        largeImageView.loadUri(path);
    }

    public static boolean isPanoramaImage(String path){
        Map<String, String> map = ExifUtil.readExif(path);
        String xml = map.get("Xmp");
        if(!TextUtils.isEmpty(xml) ){
            if(xml.contains("GPano:UsePanoramaViewer")){
                return true;
            }
        }
        return false;
    }

    public void loadUrl2(View view) {
        ImageLoader.with(this)
                .url("http://kodo.hss01248.tech/testimg/tmp-splash_stars.jpeg")
                // .url("http://img.yxbao.com/news/image/201703/13/7bda462477.gif")
                // .res(R.drawable.thegif)
                .defaultErrorRes(true)
                .placeHolder(R.mipmap.ic_launcher, false)
                .into(ivUrl);
    }

    public void loadFile2(String path) {
        ImageLoader.with(this)
                .file(path)
                //.url("http://kodo.hss01248.tech/testimg/tmp-splash_stars.jpeg")
                // .url("http://img.yxbao.com/news/image/201703/13/7bda462477.gif")
                // .res(R.drawable.thegif)
                .defaultErrorRes(true)
                .placeHolder(R.mipmap.ic_launcher, false)
                .into(ivUrl);
        ivUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LargeImageViewer.showBig()
                LargeImageViewer.showInDialog(path);

            }
        });
    }

    public void loadFile3(View view) {
        ImgDataSeletor.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
            @Override
            public void onSuccess(String path) {
                String s = AddByteUtil.addByte(path);
                loadFile2( s);

                File tmpOriginalFile = AddByteUtil.createTmpOriginalFile(s);
                /*if(path.contains(".avif")){
                    subsamplingScaleImageView.setBitmapDecoderClass(AvifSubsamplingImageDecoder.class);
                    subsamplingScaleImageView.setRegionDecoderClass(AvifSubsamplingImageRegionDecoder.class);
                }else {
                    subsamplingScaleImageView.setBitmapDecoderClass(SkiaImageDecoder.class);
                    subsamplingScaleImageView.setRegionDecoderClass(SkiaImageRegionDecoder.class);
                }*/
                subsamplingScaleImageView.setMaxScale(12);
                subsamplingScaleImageView.setDebug(AppUtils.isAppDebug());
                subsamplingScaleImageView.setOnStateChangedListener(new SubsamplingScaleImageView.OnStateChangedListener() {
                    @Override
                    public void onScaleChanged(float newScale, int origin) {
                        LogUtils.d("onScaleChanged",newScale,origin);
                    }

                    @Override
                    public void onCenterChanged(PointF newCenter, int origin) {
                        LogUtils.d("onScaleChanged",newCenter,origin);
                    }
                });

                subsamplingScaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
                    @Override
                    public void onReady() {
                        LogUtils.d("onReady");
                    }

                    @Override
                    public void onImageLoaded() {
                        LogUtils.d("onImageLoaded");
                    }

                    @Override
                    public void onPreviewLoadError(Exception e) {
                        LogUtils.w("onPreviewLoadError",e);
                    }

                    @Override
                    public void onImageLoadError(Exception e) {
                        LogUtils.w("onImageLoadError",e);
                    }

                    @Override
                    public void onTileLoadError(Exception e) {
                        LogUtils.w("onTileLoadError",e);
                    }

                    @Override
                    public void onPreviewReleased() {
                        LogUtils.d("onPreviewReleased");
                    }
                });




                //subsamplingScaleImageView.setImage(ImageSource.uri(Uri.fromFile(tmpOriginalFile)));
            }

            @Override
            public void onFail(String path, String msg) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void hideDir(View view) {
        ImgDataSeletor.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
            @Override
            public void onSuccess(String path) {
                File file = new File(path);
                if(!file.isDirectory()){
                    ToastUtils.showLong("请选择一个文件夹");
                    return;
                }
                DirOperationUtil.hideDirAndInnerFiles(file);
                ToastUtils.showLong("然后显示第一张图片");
                File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().contains(".jpg");
                    }
                });
                loadFile2(files[0].getAbsolutePath());

            }

            @Override
            public void onFail(String path, String msg) {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void downloadDemo(View view) {
        startActivity(new Intent(this, DownloadDemoActivity.class));
    }

    public void askManagerMediaPermission(View view) {
        FileDeleteUtil.checkMediaManagerPermission(new Runnable() {
            @Override
            public void run() {

            }
        },null);
    }

    public void viewDir(View view) {
        FileTreeViewHolder.viewDirInActivity(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public void motionPhoto(View view) {

        MediaPickUtil.pickImage(new MyCommonCallback<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Map<String, Object> metadata = MotionPhotoUtil.metadata(uri.toString());
                // Gson gson = new GsonBuilder().setPrettyPrinting().create();
                //String json = gson.toJson(metadata);
                FullScreenDialogUtil.showMap("meta",metadata);
            }
        });

    }



    /*Intent intent = new Intent(this,BigImageActy.class);
        startActivity(intent);*/

}
