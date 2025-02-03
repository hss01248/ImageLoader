package com.hss01248.bigimageviewpager;

import android.view.View;

import java.util.List;

/**
 * @Despciption todo
 * @Author hss
 * @Date 6/19/24 3:29 PM
 * @Version 1.0
 */
public interface OnRightMenuClickedListener {

    void onClicked(View view, String path, List<String> paths,int position);
}
