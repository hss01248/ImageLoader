package com.konstantinschubert.writeinterceptingwebview;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


class Utils {

    static byte[] consumeInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = inputStream.read(buffer)) != -1; ) {
            byteArrayOutputStream.write(buffer, 0, count);
        }
        return byteArrayOutputStream.toByteArray();
    }


}
