package com.hss01248.glidev4.config;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.FileLoader;

import java.io.File;

/**
 * @Despciption todo
 * @Author hss
 * @Date 28/07/2023 19:33
 * @Version 1.0
 */
public class MyFileLoader<Data> extends FileLoader<Data> {
    public MyFileLoader(FileOpener<Data> fileOpener) {
        super(fileOpener);
    }

    @Override
    public LoadData<Data> buildLoadData(@NonNull File model, int width, int height, @NonNull Options options) {
        return super.buildLoadData(model, width, height, options);
    }
}
