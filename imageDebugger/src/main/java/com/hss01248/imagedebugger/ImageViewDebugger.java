package com.hss01248.imagedebugger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.load.resource.gif.GifDrawable;


import java.io.File;

public class ImageViewDebugger {

    public static void enableDebug(ImageView imageView,IImageSource imageSource){
        imageView.setTag(R.id.img_debugger_view_id,imageSource);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }
                if (event.getX() > SizeUtils.dp2px(25) || event.getY() > SizeUtils.dp2px(25)) {
                    return false;
                }
               Object tag =  v.getTag(R.id.img_debugger_view_id);
                if(!(tag instanceof IImageSource)){
                    return false;
                }
                IImageSource source = (IImageSource) tag;
                showPop((ImageView) v, source);
                return false;
            }
        });

    }

    private static void showPop(ImageView v ,IImageSource imageSource) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
        ScrollView scrollView = new ScrollView(v.getContext());
        LinearLayout linearLayout = new LinearLayout(v.getContext());
        scrollView.addView(linearLayout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final TextView textView = new TextView(v.getContext());
        String desc = imageSource.getUri() + "\n\n";
        desc += "load cost :" + imageSource.getCost() + "ms\n\n";

        String errorDes = imageSource.getErrorDes();
        if (!"null".equals(errorDes) && !TextUtils.isEmpty(errorDes)) {
            desc += errorDes + "\n\n";
        }

        Drawable drawable = v.getDrawable();
       /* if(drawable instanceof GlideBitmapDrawable){
            GlideBitmapDrawable glideBitmapDrawable = (GlideBitmapDrawable) drawable;
            Bitmap bitmap = glideBitmapDrawable.getBitmap();
            desc += MyUtil.printBitmap(bitmap)+"\n";
            if (MyUtil.isBitmapTooLarge(bitmap.getWidth(),bitmap.getHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
        }else */
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            desc += ImageDebugUtil.printBitmap(bitmap) + "\n";
            if (ImageDebugUtil.isBitmapTooLarge(bitmap.getWidth(), bitmap.getHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
        } else /*if (drawable instanceof SquaringDrawable){
            SquaringDrawable bitmap = (SquaringDrawable) drawable;
            desc += "\nSquaringDrawable, w:"+bitmap.getIntrinsicWidth() +",h:"+bitmap.getIntrinsicHeight()+"\n";
            if (MyUtil.isBitmapTooLarge(bitmap.getIntrinsicWidth(),bitmap.getIntrinsicHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
        }else*/ if (drawable instanceof GifDrawable) {
            GifDrawable gifDrawable = (GifDrawable) drawable;
            //Grow heap (frag case) to 74.284MB for 8294412-byte allocation
            desc += "gif :" + gifDrawable.getIntrinsicWidth() + "x" + gifDrawable.getIntrinsicHeight() + "x" + gifDrawable.getFrameCount();

            if (ImageDebugUtil.isBitmapTooLarge(gifDrawable.getIntrinsicWidth(), gifDrawable.getIntrinsicHeight(), v)) {
                textView.setTextColor(Color.RED);
            }
            if (gifDrawable.getFrameCount() > 10) {
                desc += "\nframeCount is too many!!!!!!!!\n";
                textView.setTextColor(Color.parseColor("#8F0005"));
            }

        } else {
            desc += "drawable:" + drawable;
        }

        desc += "\n" + ImageDebugUtil.printImageView(v);

        textView.setText(desc);

        imageSource.getLocalFilePath(new IImgLocalPathGetter(imageSource.getUri()) {
            @Override
            public void onGet(File file) {
                String text = textView.getText().toString();
                text += "\n\n" + ImageDebugUtil.printExif(file.getAbsolutePath());
                textView.setText(text);
            }

            @Override
            public void onError(Throwable e) {
                String text = textView.getText().toString();
                text += "\n\n get cache file failed :\n";
                if (e != null) {
                    text += e.getClass().getName() + " " + e.getMessage();
                }

                textView.setText(text);
            }
        });


        textView.setPadding(20, 20, 20, 20);

        ImageView imageView = new ImageView(v.getContext());
        imageView.setImageDrawable(drawable);
        linearLayout.addView(imageView);
        linearLayout.addView(textView);
        linearLayout.setPadding(10, 30, 10, 20);


        dialog.setView(scrollView);
        dialog.setPositiveButton("拷贝链接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ImageDebugUtil.copyText(imageSource.getUri());
                Toast.makeText(textView.getContext(), "已拷贝链接", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setNegativeButton("拷贝，并在浏览器中打开此链接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    ImageDebugUtil.copyText(imageSource.getUri());
                    Toast.makeText(textView.getContext(), "已拷贝链接", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri content_url = Uri.parse(imageSource.getUri());
                    intent.setData(content_url);
                    textView.getContext().startActivity(intent);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        });
        dialog.show();

    }
}
