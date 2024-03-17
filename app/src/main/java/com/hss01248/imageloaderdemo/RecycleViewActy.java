package com.hss01248.imageloaderdemo;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.hss01248.adapter.SuperRvAdapter;
import com.hss01248.adapter.SuperRvHolder;
import com.hss01248.image.ImageLoader;
import com.hss01248.image.config.ScaleMode;
import com.hss01248.image.utils.ImageLoaderDebugTool;
import com.hss01248.imageloaderdemo.multi.RcvHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangshuisheng on 2017/9/28.
 */

public class RecycleViewActy extends AppCompatActivity {

    RecyclerView recyclerView;
    SuperRvAdapter adapter;
    List<String> datas;
    int oldScrollState;

    ImageView iv1, iv2, iv3, iv4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acty_recycle_list);

        recyclerView = (RecyclerView) findViewById(R.id.recy);
        iv1 = findViewById(R.id.iv_1);
        iv2 = findViewById(R.id.iv_2);
        iv3 = findViewById(R.id.iv_3);
        iv4 = findViewById(R.id.iv_4);

        datas = new ArrayList<>();
        addDatas();
       // ImageLoaderDebugTool.warnBigBitmapInCurrentViewTree(recyclerView);


        adapter = new SuperRvAdapter(this) {
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                super.onBindViewHolder((SuperRvHolder) holder,position);
            }

            @Override
            protected SuperRvHolder generateCoustomViewHolder(ViewGroup viewGroup, int i) {
                return new RcvHolder(
                        View.inflate(RecycleViewActy.this, R.layout.item_iv, null));//.setColumnNum(3);
            }
        };
        adapter.refresh(datas);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
       /* StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);*/
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
       /* recyclerView.addItemDecoration(new DividerItemDecoration(
            this, DividerItemDecoration.HORIZONTAL));*/
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // ImageLoader.getActualLoader().resume();
                } else {
                    // ImageLoader.getActualLoader().pause();
                }
            }
        });

        setIv();
    }

    private void setIv() {
        ImageLoader.with(this)
                .url("http://www.cingjing.gov.tw/upload/grass_b/0898a3792067e612b0a87f3c5058dae6.jpg")
                .scale(ScaleMode.CENTER_CROP)
                .into(iv1);

    }

    private void addDatas() {
        datas.add("https://c-ssl.duitang.com/uploads/blog/201407/04/20140704234425_j5zHS.thumb.700_0.gif");
        datas.add("http://www.cingjing.gov.tw/upload/grass_b/0898a3792067e612b0a87f3c5058dae6.jpg");
        datas.add("http://www.cingjing.gov.tw/upload/grass_b/c7c860cb7eb08a85e1db34ccd68b0891.jpg");
        datas.add("http://ww2.sinaimg.cn/large/85cc5ccbgy1fi7jt5or6sg20rs0ku4qr.jpg");//gif
        datas.add("http://image.wenweipo.com/2017/02/09/20170209zf0010.jpg");
        datas.add("http://qntemp.bejson.com/upload/15942127644639826Xnip2019-07-31_18-21-49.jpg?imageView2/0/format/webp");
        datas.add("http://img.mp.itc.cn/upload/20170101/43c903083d904a58a8d9e06511c5641a_th.png");
        datas.add("http://images.epochweekly.com/493/66-01.jpg");

        //巨图
        datas.add("https://s3.51cto.com/wyfs02/M02/06/ED/wKiom1nAst7gJXLWAApAOtlw0r4105.jpg");
        datas.add("https://s3.51cto.com/wyfs02/M00/A5/A5/wKioL1nA-WrQ8NSkAADpAlDnsrM054.jpg");
        datas.add("https://s2.51cto.com/wyfs02/M02/06/F4/wKiom1nA9iSRwF1BADe7ZVL2w4Q127.jpg");


        datas.add("https://img.zcool.cn/community/01f1bc58413d49a8012060c80de125.gif");
        datas.add("http://pix10.agoda.net/hotelImages/271/271354/271354_17080815220055007573.jpg?s=1024x768");
        datas.add("https://d12x8ezp3au6m3.cloudfront.net/item/yEDHKC4jp_mPfaUr91viPoY9npCFf032oVJ021WbQN4.png");
        datas.add("http://n.sinaimg.cn/translate/20170725/DYCJ-fyiiahy9953034.jpg");
        datas.add("http://i4.download.fd.pchome.net/g1/M00/08/13/ooYBAFOVe3KICOm0AAigSrK-BVoAABlqQHJDioACKBi325.jpg");
        datas.add("http://n.sinaimg.cn/translate/20170427/b3dI-fyeqcac2218956.jpg");
        datas.add("http://file26.mafengwo.net/M00/D9/19/wKgB4lKSHxuAb9hgAAfVXrwAehY28.jpeg");
        datas.add("https://i.ytimg.com/vi/6kry0lLwT9I/maxresdefault.jpg");
        datas.add("http://i3.download.fd.pchome.net/t_1680x1050/g1/M00/08/13/oYYBAFOVe6iIKdIxAAwl3ATECJoAABlqQLL4_0ADCX0000.jpg");
        datas.add("https://www.nmmts.com/uploadfile/2017/0614/20170614052945147.png");
        datas.add("http://www.shengskj.com/uploads/allimg/161123/8-161123160021591.jpg");
        datas.add("https://s.yimg.com/lo/api/res/1.2/jiIJkFyzGzLBTr5Ec2KgEQ--/YXBwaWQ9eXR3ZnBhZ2U7dz02NDA7cT03NTtzbT0x/http://media.zenfs.com/zh-Hant-TW/homerun/twautos.carnews.com/8ad60b65d4e9e5b18219da19f718fefd");
        datas.add("http://www.dm4j.com/pic/20170215/17/article_4503317_8f66f5072908ad15430822.jpg");
        datas.add("http://www.mjjq.com/pic/maps_images/1150106296_1407667555.jpg");
        datas.add("http://pic4.40017.cn/scenery/destination/2016/08/26/14/LVWkTh.jpg");
        datas.add("http://img.cyw.com/temp/shopinfo/2016051017135144.jpg");
        datas.add("http://www.jinyongwang.com/public/uploads/2015-10-08/56160185e94ea.jpg");
        datas.add("http://www.twwiki.com/uploads/wiki/ac/aa/719237_28.jpg");
        datas.add("http://www.doit01.com/data/u11606/public/201706/20170615/2017061514374293300.png");
        datas.add("http://oushidai.wangdk.cn/static/upload/2017/03/01/20170301122725000000_1_259862_68.jpg");
        datas.add("http://www.gzhphb.com/gpPic/600/0/mmbiz.qpic.cn/mmbiz_jpg/zSREKq4KgM212M0Vcj0iaYXs3dZbQXLKdau3ubBvCzsh5UdScI5tzeF9tctVBIF9XuXib04aG3qDBibJ63icX6wUVg/0?wx_fmt=jpeg");
        datas.add("https://dimg07.c-ctrip.com/images/fd/tg/g3/M05/96/85/CggYGlZMM7yAcLXJADOLWmkNmqw824_C_880_350.jpg");
        datas.add("https://img1.qunarzz.com/travel/poi/1509/8d/80c6fd92deb355.jpg");
        datas.add("http://www.wyren.net/uploads/allimg/c100820/12R2ZX440M0-D4Z.jpg");
        datas.add("http://www.wyren.net/uploads/allimg/160402/1_160402152845_1.jpg");
        datas.add("http://www.northwest.com.tw/travel/images/travel/2c763bdc7ef69828bbd9ae6362e000e1.jpg");
        datas.add("http://www.tourking.com.tw/travelpic/27633t.jpg");
        datas.add("http://img.yilvcheng.com/Public/upload/goods/58d9d2178bac3.jpg");
        datas.add("http://dlgjlx.com/uploads/allimg/20140521/1405210G226.jpg");
        datas.add("http://信用新疆.com/img/index/fc2.png");
        datas.add("http://gmimg.geimian.com/pic/2015/03/20150317_192226_013geimian1.jpg");
        datas.add("http://img1.readhouse.net/20161124/147998389276531.jpg");
        datas.add("http://img.mp.itc.cn/upload/20170504/e68ee494f29d4c3783503f90a7d53cbc_th.jpg");
        datas.add("http://i443.photobucket.com/albums/qq156/deutschina_001/1256492488.jpg?t=1256499842");
        datas.add("https://icrvb3jy.xinmedia.com/solomo/article/4734/4D81A9AB-CB7B-4786-A28D-B07B80C279F8.jpg");
        datas.add("http://www.zgscyl.com/UploadFiles/Works/20160322/20160322173023292329.jpg");
        datas.add("http://i1.17173.itc.cn/2012/9yin/2012/08/jietu/swdm_2.jpg");
        datas.add("http://photo.ts.cn/content/attachement/jpg/site1/20140512/002197cf8da514dae2b228.jpg");
        datas.add("http://img95.699pic.com/photo/50038/5933.jpg_wh860.jpg");
        datas.add("http://upload-images.jianshu.io/upload_images/3928113-ccb0943eb63b78dc.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240");
        datas.add("https://i2.wp.com/chunyingtravel.com/wp-content/uploads/2015/06/Day-2-Toilet-near-the-Ger-Camp.jpg");
        datas.add("http://image.juntu.com/uploadfile/2017/0818/20170818095821327.jpg");
        datas.add("https://b1-q.mafengwo.net/s6/M00/D2/55/wKgB4lOSj5-ATEDcAAHO0z7f3v887.jpeg?imageView2%2F2%2Fw%2F600%2Fq%2F90");


        //adapter.refresh(datas);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
