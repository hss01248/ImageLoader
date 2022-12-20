package com.hss01248.basewebview.menus;

import android.webkit.WebView;

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

        String[] items = {"扫码"};
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
                                }
                            }
                        });
    }
}
