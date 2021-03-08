package com.hss01248.imageloaderdemo;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import android.widget.TextView;

import com.hss01248.image.ImageLoader;

import java.util.ArrayList;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/3/24 0024.
 */

public class ViewpagerActy extends Activity {


    @BindView(R.id.index)
    TextView tvIndex;
    @BindView(R.id.viewpager)
    ViewPager viewPager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //http://www.deskcity.org/nature-and-landscape/
        setContentView(R.layout.activity_viewpager);
        ButterKnife.bind(this);
        //viewPager = (ViewPager) findViewById(R.id.viewpager);
        List<String> urls = new ArrayList<>();
       /* urls.add("/storage/emulated/0/DCIM/家里有用图/IMG_20170222_221249_HHT.jpg");
        urls.add("/storage/emulated/0/DCIM/家里有用图/IMG_20161114_231649.jpg");
        urls.add("/storage/emulated/0/DCIM/家里有用图/IMG_20161229_221023.jpg");
        urls.add("/storage/emulated/0/DCIM/家里有用图/DSC_0051.JPG");*/
        urls.add("http://img.daimg.com/uploads/allimg/180209/3-1P209124408.jpg");//这张图片有问题
        urls.add("https://c-ssl.duitang.com/uploads/blog/201407/04/20140704234425_j5zHS.thumb.700_0.gif");
        urls.add("http://pic.netbian.com/uploads/allimg/180826/113958-153525479855be.jpg");
        urls.add("http://pic.netbian.com/uploads/allimg/191022/000653-1571674013ac2d.jpg");
        urls.add("http://pic.netbian.com/uploads/allimg/191015/231304-1571152384273d.jpg");
        urls.add("https://images2.alphacoders.com/751/thumb-1920-751214.jpg");
        //巨图
        urls.add("https://s3.51cto.com/wyfs02/M02/06/ED/wKiom1nAst7gJXLWAApAOtlw0r4105.jpg");
        urls.add("https://s3.51cto.com/wyfs02/M00/A5/A5/wKioL1nA-WrQ8NSkAADpAlDnsrM054.jpg");
        urls.add("https://s2.51cto.com/wyfs02/M02/06/F4/wKiom1nA9iSRwF1BADe7ZVL2w4Q127.jpg");

        urls.add("http://up.deskcity.org/pic_source/e5/33/0b/e5330b32f2221ffebb34f4d662c7b1c2.jpg");
        urls.add("http://up.deskcity.org/pic_source/20/7f/28/207f2829d593e1cbfcdbc82490ee238d.jpg");
        urls.add("http://up.deskcity.org/pic_source/af/6d/a7/af6da7cfcfb6ec554fd2fe532f36f743.jpg");
        urls.add("http://up.deskcity.org/pic_source/2b/0d/ba/2b0dba8691592753630dbf28e09eb3ec.jpg");
        urls.add("http://up.deskcity.org/pic_source/a5/bd/4e/a5bd4eccad6ade1915036c3d1b6a8d56.jpg");
        urls.add("http://up.deskcity.org/pic_source/b6/07/97/b6079776a38480558cbbeb7e77e790a0.jpg");
        urls.add("http://up.deskcity.org/pic_source/e4/19/0f/e4190f73ee3bdf84cbd905bbef2d5acd.jpg");
        urls.add("http://up.deskcity.org/pic_source/5e/27/98/5e2798e6d9c0e8589958d70483217704.jpg");
        urls.add("http://up.deskcity.org/pic_source/8f/a4/00/8fa400c4b17f40687fa2860d01be6d1c.jpg");
        urls.add("http://up.deskcity.org/pic_source/7d/57/e5/7d57e55214cb93a5717720d7a01c9831.jpg");
        urls.add("http://up.deskcity.org/pic_source/b8/62/b4/b862b432a747e82f78b004381f67a16b.jpg");
        urls.add("http://up.deskcity.org/pic_source/79/5d/b2/795db2bfd4edbfe8b6796f706aca2b60.jpg");


        // PagerAdapterForBigImage adapter = new PagerAdapterForBigImage(urls);
        // viewPager.setAdapter(adapter);
        ImageLoader.loadBigImages(viewPager, urls);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {


            }

            @Override
            public void onPageSelected(int i) {
                tvIndex.setText("index:" + i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //viewPager.destroyDrawingCache();
        ImageLoader.clearAllMemoryCaches();//调了没用,也不需要调,下次进来自动会刷新内存
    }
}
