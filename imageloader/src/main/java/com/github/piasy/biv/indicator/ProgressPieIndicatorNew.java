/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.piasy.biv.indicator;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.piasy.biv.view.BigImageView;
import com.hss01248.image.R;

import java.util.Locale;

/**
 * Created by Piasy{github.com/Piasy} on 12/11/2016.
 */

public class ProgressPieIndicatorNew implements ProgressIndicator {
    private RelativeLayout root;

    private TextView tvProgress;


    @Override
    public View getView(BigImageView parent) {
        root = (RelativeLayout) View.inflate(parent.getContext(),
                parent.isDarkTheme() ? R.layout.ui_progress_pie_indicator_new_dark : R.layout.ui_progress_pie_indicator_new,null);

        tvProgress = root.findViewById(R.id.tv_progress);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.setLayoutParams(params);
        return root;
    }

    @Override
    public void onStart() {
        // not interested
        root.setVisibility(View.VISIBLE);

    }

    @Override
    public void onProgress(int progress) {
        if (progress < 0 || progress > 100) {
            return;
        }
        tvProgress.setText(String.format(Locale.getDefault(), "%d%%", progress));
    }

    @Override
    public void onFinish() {
        root.setVisibility(View.GONE);
        // not interested
    }
}
