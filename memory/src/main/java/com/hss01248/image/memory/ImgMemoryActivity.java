package com.hss01248.image.memory;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * time:2019/10/22
 * author:hss
 * desription:
 */
public class ImgMemoryActivity extends Activity {

    RecyclerView listView;
    Timer timer;
    TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_memorylist);
        listView = findViewById(R.id.lv);
        textView = findViewById(R.id.tv_imgs_imfo);

        List<Bitmap> bitmaps = ImageMemoryHookManager.getList();
        setImgsInfo(bitmaps);
        /*if(bitmaps == null || bitmaps.isEmpty()){
            Log.w("ImgMemoryActivity","ImageMemoryHookManager.getList().isEmpty");
            Toast.makeText(getApplicationContext(),"ImageMemoryHookManager.getList().isEmpty",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }*/

        if (bitmaps == null) {
            bitmaps = new ArrayList<>();
        }
        final BaseQuickAdapter adapter = new ImgItem(R.layout.img_item_show, bitmaps);
        listView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<Bitmap> bitmaps = ImageMemoryHookManager.getList();
                        setImgsInfo(bitmaps);
                        adapter.replaceData(bitmaps);
                    }
                });


            }
        }, 3000, 3000);


    }

    private void setImgsInfo(List<Bitmap> bitmaps) {
        if (bitmaps == null || bitmaps.isEmpty()) {
            textView.setText("ImageMemoryHookManager.getList().isEmpty");
            return;
        }
        long size = 0;
        for (Bitmap bi :
                bitmaps) {
            size += ImageMemoryHookManager.getSize(bi);
        }
        textView.setText("bitmap count:" + bitmaps.size() + ",占用内存共:" + ImageMemoryHookManager.formatFileSize(size));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }

    }
}
