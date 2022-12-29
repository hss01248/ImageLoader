package com.hss01248.basewebview;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.hss.utils.enhance.viewholder.MyViewHolder;
import com.hss01248.basewebview.databinding.TitlebarForWebviewBinding;
import com.hss01248.toast.MyToast;

/**
 * @Despciption todo
 * @Author hss
 * @Date 29/12/2022 16:23
 * @Version 1.0
 */
public class WebViewTitlebarHolder extends MyViewHolder<TitlebarForWebviewBinding,BaseQuickWebview> {
    public void setFullWebBrowserMode(boolean fullWebBrowserMode) {
        isFullWebBrowserMode = fullWebBrowserMode;
        handleBrowserMode();
    }

    @Override
    protected TitlebarForWebviewBinding createBinding(ViewGroup parent) {
        return TitlebarForWebviewBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
    }

    /**
     * 一般是普通查看网页模式
     * 可以切换为浏览器模式-WebBrowserMode
     */
    boolean isFullWebBrowserMode = false;
    BaseQuickWebview quickWebview;
    public WebViewTitlebarHolder(ViewGroup parent) {
        super(parent);
    }

    @Override
    protected void assignDataAndEventReal(BaseQuickWebview data) {
        quickWebview = data;
        handleBrowserMode();
        binding.ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.showMenu();
            }
        });
    }

    private void handleBrowserMode() {
        if(isFullWebBrowserMode){
            binding.ivClose.setVisibility(View.GONE);
            binding.tvTitle.setEnabled(true);
            binding.tvTitle.setFocusable(true);
            binding.tvTitle.setFocusableInTouchMode(true);
            binding.ivBack.setImageResource(R.drawable.icon_website_default);
            binding.ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyToast.debug("todo ");
                }
            });
            binding.ivRightRefresh.setVisibility(View.VISIBLE);
            binding.ivRightRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quickWebview.getWebView().reload();
                }
            });
      /*      binding.tvTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    showIconToClear(hasFocus);
                }
            });*/
            binding.tvTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showIconToClear(true);
                }
            });

            binding.tvTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_SEARCH){
                        String str = binding.tvTitle.getText().toString().trim();
                       if(TextUtils.isEmpty(str)){
                           return false;
                       }
                       if(str.startsWith("http")){
                           quickWebview.loadUrl(str);
                           KeyboardUtils.hideSoftInput(binding.tvTitle);
                           showIconToClear(false);
                           return true;
                       }
                       //调用百度/谷歌搜索
                        String url = "https://www.baidu.com/s?wd="+str;
                       quickWebview.loadUrl(url);
                        showIconToClear(false);
                        KeyboardUtils.hideSoftInput(binding.tvTitle);
                        return true;
                    }
                    return false;
                }
            });


        }else {
            binding.ivRightRefresh.setVisibility(View.GONE);
            binding.ivClose.setVisibility(View.VISIBLE);
            binding.tvTitle.setEnabled(false);
            binding.tvTitle.setFocusable(false);
            binding.tvTitle.setFocusableInTouchMode(false);
            binding.ivBack.setImageResource(R.drawable.comm_title_back);
            binding.ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean b = quickWebview.onBackPressed();
                    if(!b){
                        ActivityUtils.getTopActivity().finish();
                    }
                }
            });
            binding.ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.getTopActivity().finish();
                }
            });
        }
    }

    private void showIconToClear(boolean hasFocus) {
        if(hasFocus){
            binding.ivRightRefresh.setImageResource(R.drawable.icon_close);
            binding.ivRightRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.tvTitle.setText("");
                }
            });
        }else {
            binding.ivRightRefresh.setImageResource(R.drawable.icon_refresh_webpage);
            binding.ivRightRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quickWebview.getWebView().reload();
                }
            });
        }
    }
}
