package com.hss01248.image.bigimage2;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.hss01248.image.ImageLoader;
import com.hss01248.image.MyUtil;
import com.hss01248.image.R;
import com.hss01248.image.interfaces.FileGetter;
import com.hss01248.pagestate.PageStateManager;
import com.shizhefei.view.largeimage.factory.FileBitmapDecoderFactory;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import pl.droidsonroids.gif.GifImageView;

public class MyLargeImageHolder {

    MyLargeImageView largeImageView;
    GifImageView gif;
    FrameLayout root;
    PageStateManager manager;

    public MyLargeImageHolder(Context context, ViewGroup viewParent) {
        root = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.holde_big_image, viewParent, false);
        largeImageView = root.findViewById(R.id.my_big_image);
        gif = root.findViewById(R.id.gifview);
    }

    public void loadImage(String source) {
        if (source.contains(".gif")) {
            largeImageView.setVisibility(View.GONE);
            gif.setVisibility(View.VISIBLE);

            if (source.startsWith("http")) {
                manager = PageStateManager.initWhenUse(largeImageView, null);
                downloadAndShow(source);
            } else if (source.startsWith("/storage/")) {
                File file = new File(source);
                gif.setImageURI(Uri.fromFile(file));
            } else {//if(source.startsWith("content"))
                gif.setImageURI(Uri.parse(source));
            }


        } else {
            largeImageView.setVisibility(View.VISIBLE);
            gif.setVisibility(View.GONE);

            if (source.startsWith("http")) {
                manager = PageStateManager.initWhenUse(largeImageView, null);
                downloadAndShow(source);
            } else if (source.startsWith("/storage/")) {
                File file = new File(source);
                largeImageView.setImage(new FileBitmapDecoderFactory(file));
            } else {//if(source.startsWith("content"))
                try {
                    largeImageView.setImage(new InputStreamBitmapDecoderFactory(
                            new FileInputStream(root.getContext().getContentResolver().openFileDescriptor(Uri.parse(source), "r").getFileDescriptor())));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void downloadAndShow(final String source) {
        manager.showLoading();
        ImageLoader.getActualLoader().download(source, new FileGetter() {
            @Override
            public void onSuccess(File file, int width, int height) {
                manager.showContent();
                if (source.contains(".gif")) {
                    gif.setImageURI(Uri.fromFile(file));
                } else {
                    if ("gif".equals(MyUtil.getRealType(file))) {
                        gif.setImageURI(Uri.fromFile(file));
                    } else {
                        largeImageView.setImage(new FileBitmapDecoderFactory(file));
                    }

                }
            }

            @Override
            public void onFail(Throwable e) {
                manager.showError(e.getMessage() + "\n" + source);
            }
        });

    }
}
