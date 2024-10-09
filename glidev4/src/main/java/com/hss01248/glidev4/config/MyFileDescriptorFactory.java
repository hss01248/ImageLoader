package com.hss01248.glidev4.config;

import android.os.ParcelFileDescriptor;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.load.model.FileLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @Despciption todo
 * @Author hss
 * @Date 28/07/2023 19:43
 * @Version 1.0
 */
public class MyFileDescriptorFactory extends FileLoader.Factory<ParcelFileDescriptor> {



    public MyFileDescriptorFactory() {
     super(
                new FileLoader.FileOpener<ParcelFileDescriptor>() {
                    @Override
                    public ParcelFileDescriptor open(File file) throws FileNotFoundException {
                        LogUtils.i("open file--->by glide: "+file.getAbsolutePath());
                        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                    }

                    @Override
                    public void close(ParcelFileDescriptor parcelFileDescriptor) throws IOException {
                        parcelFileDescriptor.close();
                    }

                    @Override
                    public Class<ParcelFileDescriptor> getDataClass() {
                        return ParcelFileDescriptor.class;
                    }
                });
    }

}
