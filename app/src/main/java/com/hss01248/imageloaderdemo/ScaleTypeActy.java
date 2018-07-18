package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.hss01248.adapter.SuperLvAdapter;
import com.hss01248.adapter.SuperLvHolder;

import java.util.ArrayList;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/11/5.
 */

public class ScaleTypeActy extends Activity {

    @BindView(R.id.gv)
    GridView gv;
    @BindView(R.id.iv_small)
    ImageView ivSmall;
    @BindView(R.id.iv_big)
    ImageView ivBig;
    @BindView(R.id.btn_loadsmall)
    Button btnLoadsmall;
    @BindView(R.id.btn_loadbig)
    Button btnLoadbig;
    SuperLvAdapter lvAdapter;
    List<ScaleTypeInfo> datas ;
    int resSmall ;
    int resBig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acty_scaletype);
        ButterKnife.bind(this);
        datas = new ArrayList<>();
        resSmall = R.drawable.timg50;
        resBig = R.drawable.timg6;
        datas.add(new ScaleTypeInfo(ImageView.ScaleType.CENTER_CROP,"center_crop",resSmall));
        datas.add(new ScaleTypeInfo(ImageView.ScaleType.CENTER,"center",resSmall));
        datas.add(new ScaleTypeInfo(ImageView.ScaleType.CENTER_INSIDE,"center_inside",resSmall));
        datas.add(new ScaleTypeInfo(ImageView.ScaleType.FIT_CENTER,"fit_center",resSmall));
        datas.add(new ScaleTypeInfo(ImageView.ScaleType.FIT_XY,"fit_xy",resSmall));
        datas.add(new ScaleTypeInfo(ImageView.ScaleType.FIT_START,"fit_start",resSmall));
        datas.add(new ScaleTypeInfo(ImageView.ScaleType.FIT_END,"fit_end",resSmall));
        datas.add(new ScaleTypeInfo(ImageView.ScaleType.MATRIX,"matrix",resSmall));



        initGv();

    }

    private void initGv() {
        gv.setNumColumns(4);
        lvAdapter = new SuperLvAdapter(this) {
            @Override
            protected SuperLvHolder generateNewHolder(Context context, int i, Class aClass) {
                return new ScaleTypeHolder(ScaleTypeActy.this);
            }

        };
        gv.setAdapter(lvAdapter);

    }

    @OnClick({R.id.btn_loadsmall, R.id.btn_loadbig})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_loadsmall:
                for(ScaleTypeInfo info : datas){
                    info.resId = resSmall;
                }
                lvAdapter.refresh(new ArrayList(datas));
                break;
            case R.id.btn_loadbig:
                for(ScaleTypeInfo info : datas){
                    info.resId = resBig;
                }
                lvAdapter.refresh(new ArrayList(datas));
                break;
        }
    }
}
