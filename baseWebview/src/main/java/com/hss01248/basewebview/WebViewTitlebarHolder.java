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
import com.blankj.utilcode.util.SizeUtils;
import com.hss.utils.enhance.viewholder.MyViewHolder;
import com.hss01248.basewebview.databinding.TitlebarForWebviewBinding;

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
            ViewGroup.LayoutParams layoutParams = binding.ivBack.getLayoutParams();
            layoutParams.width = SizeUtils.dp2px(15);
            layoutParams.height = SizeUtils.dp2px(15);
            binding.ivBack.setLayoutParams(layoutParams);
            binding.tvTitle.setEnabled(true);
            binding.tvTitle.setFocusable(true);
            binding.tvTitle.setFocusableInTouchMode(true);
            binding.ivBack.setImageResource(R.drawable.icon_website_default);
            binding.ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //MyToast.debug("todo ");
                }
            });
            binding.ivRightRefresh.setVisibility(View.VISIBLE);
            binding.ivRightRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quickWebview.getWebView().reload();
                }
            });
            binding.etTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    toShowEditText(hasFocus);
                }
            });


            binding.etTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_SEARCH){
                        String str = binding.etTitle.getText().toString().trim();
                       if(TextUtils.isEmpty(str)){
                           return false;
                       }
                        quickWebview.loadUrl(str);
                        KeyboardUtils.hideSoftInput(binding.etTitle);
                        toShowEditText(false);
                        return true;
                    }
                    return false;
                }
            });
        }else {
            binding.ivRightRefresh.setVisibility(View.GONE);
            binding.ivClose.setVisibility(View.VISIBLE);
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

        binding.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFullWebBrowserMode){
                    //MyToast.debug("not isFullWebBrowserMode ");
                    return;
                }
                toShowEditText(true);
                binding.etTitle.requestFocus();
            }
        });
    }

    private void toShowEditText(boolean hasFocus) {
        showIconToClear(hasFocus);
        binding.etTitle.setVisibility(hasFocus ? View.VISIBLE:View.GONE);
        binding.tvTitle.setVisibility(hasFocus ? View.GONE:View.VISIBLE);
        if(hasFocus){
            if(!TextUtils.isEmpty(quickWebview.getCurrentUrl())){
                binding.etTitle.setText(quickWebview.getCurrentUrl());
            }
        }else {
            if(!TextUtils.isEmpty(quickWebview.getCurrentTitle())){
                binding.tvTitle.setText(quickWebview.getCurrentTitle());
            }
        }
    }

    private void showIconToClear(boolean hasFocus) {
        if(hasFocus){
            binding.ivRightRefresh.setImageResource(R.drawable.icon_close);
            binding.ivRightRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.etTitle.setText("");
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
