package com.hss01248.image.memory;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ListView;

import kale.adapter.CommonAdapter;
import kale.adapter.item.AdapterItem;

/**
 * time:2019/10/22
 * author:hss
 * desription:
 */
public class ImgMemoryActivity extends Activity {

    ListView listView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.lv);

        listView.setAdapter(new CommonAdapter<Bitmap>(ImageMemoryHookManager.getList(),1) {
            @NonNull
            @Override
            public AdapterItem createItem(Object type) {
                return new ImgItem();
            }
        });
    }
}
