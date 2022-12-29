package com.hss01248.basewebview.menus;

import android.webkit.WebView;

import com.blankj.utilcode.util.ActivityUtils;
import com.hss.utils.enhance.intent.ShareUtils;
import com.hss01248.basewebview.BaseQuickWebview;
import com.hss01248.basewebview.IShowRightMenus;
import com.hss01248.iwidget.singlechoose.SingleChooseDialogImpl;
import com.hss01248.iwidget.singlechoose.SingleChooseDialogListener;
import com.hss01248.qrscan.ScanCodeActivity;

import io.reactivex.functions.Consumer;

/**
 * @Despciption todo
 * @Author hss
 * @Date 20/12/2022 18:06
 * @Version 1.0
 */
public class DefaultMenus implements IShowRightMenus {
    @Override
    public void showMenus(WebView view, BaseQuickWebview quickWebview) {

        String[] items = {"扫码","收藏当前网页","查看收藏","分享","切换为全功能浏览器模式"};
        new SingleChooseDialogImpl()
                .showInPopMenu(quickWebview.getTitleBar().ivMenu, -1,
                        items,
                        new SingleChooseDialogListener() {
                            @Override
                            public void onItemClicked(int position, CharSequence text) {
                                if(position ==0){
                                    ScanCodeActivity.scanForResult(new Consumer<String>() {
                                        @Override
                                        public void accept(String s) throws Exception {
                                            quickWebview.loadUrl(s);
                                        }
                                    });
                                }else if(position ==4){
                                    quickWebview.getTitlebarHolder().setFullWebBrowserMode(true);
                                }
                                else if(position == 3){
                                    ShareUtils.shareMsg(ActivityUtils.getTopActivity(),"分享到",
                                            "网页分享: "+ quickWebview.getCurrentTitle(),
                                            quickWebview.getCurrentUrl(), null);
                                }
                            }
                        });
    }
}
