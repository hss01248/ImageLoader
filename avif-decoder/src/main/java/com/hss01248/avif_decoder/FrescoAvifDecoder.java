package com.hss01248.avif_decoder;

import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.QualityInfo;

/**
 * @Despciption avif-fresco
 * https://github.com/facebook/fresco/issues/2474
 *
 * 要自己写decoder
 *
 * 有字节写好的: 火山引擎sdk
 *
 * https://www.volcengine.com/docs/508/65969
 *
 * https://github.com/skyNet2017/BDFrescoDemo
 *
 * https://artifact.bytedance.com/repository/Volcengine/com/bytedance/fresco/
 * @Author hss
 * @Date 05/05/2022 11:24
 * @Version 1.0
 */
public class FrescoAvifDecoder implements ImageDecoder {
    @Override
    public CloseableImage decode(
            EncodedImage encodedImage,
            int length,
            QualityInfo qualityInfo,
            ImageDecodeOptions options) {
        // Decode the given encodedImage and return a
        // corresponding (decoded) CloseableImage.
        //CloseableImage closeableImage = ...;
        return null;
    }
}
