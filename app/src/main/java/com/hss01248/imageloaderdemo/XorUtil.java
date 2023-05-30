package com.hss01248.imageloaderdemo;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Despciption todo
 * @Author hss
 * @Date 11/04/2023 09:54
 * @Version 1.0
 */
public class XorUtil {

    public static byte[] endecrypt(int seed, byte[] bytes) {//seed为加密种子，str为加密/解密对象
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= seed;
        }
        return bytes;
    }

    public static String filePrefix = "enx-";

    public static String endecrypt(int seed, String str) {//seed为加密种子，str为加密/解密对象
        byte[] bytes = null;
        boolean isBase64 = false;

        if(str.startsWith("b64-")){
            isBase64 = true;
            str = str.substring("b64-".length());
            bytes = EncodeUtils.base64Decode(str);
        }else {
            bytes = str.getBytes(StandardCharsets.UTF_8);
        }

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= seed;
        }
        if(!isBase64){
            return   "b64-"+EncodeUtils.base64Encode2String(bytes);
        }
        return new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
    }

    public static boolean isEncrypted(String path){
        if(path.startsWith(filePrefix)){
            return true;
        }
        if(path.startsWith("b64-")){
            return true;
        }
        return false;
    }

    public static File endecrypt(int seed, File file, boolean encryptFileName) {//seed为加密种子，str为加密/解密对象
        long start = System.currentTimeMillis();
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            String sourceFileUrl = file.getAbsolutePath();
            String fileName = file.getName();

            String targetFileName = "";
            if(encryptFileName){
                targetFileName = endecrypt(seed,
                        fileName.substring(0,fileName.lastIndexOf(".")))
                        +fileName.substring(fileName.lastIndexOf("."));
            }else {
                if(fileName.startsWith(filePrefix)){
                    targetFileName = fileName.substring(filePrefix.length());
                }else {
                    targetFileName = filePrefix+fileName;
                }
            }

            String targetFileUrl = new File(file.getParentFile(), targetFileName).getAbsolutePath();
            in = new FileInputStream(sourceFileUrl);
            out = new FileOutputStream(targetFileUrl);

            int data = 0;
            while ((data = in.read()) != -1) {
//将读取到的字节异或上一个数，加密输出
                out.write(data ^ seed);
            }
            return new File(targetFileUrl);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
//在finally中关闭开启的流
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            LogUtils.d("xor file cost: ms: "+(System.currentTimeMillis()- start));
            //xor file cost: ms: 14572
        }
    }
}
