package com.hss01248.motion_photos;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MotionPhotoUtil {

    public static void setMotion(IMotion motion) {
        MotionPhotoUtil.motion = motion;
    }

    static IMotion motion = new JavaMotion();

    public static void main(String[] args) {
        String xiaomi = "/Users/hss/Documents/live_photos/IMG_3919.heic";
        String google = "/Users/hss/Documents/PXL_20240918_013738178.MP.jpg";
        boolean isMotionImage = isMotionImage(xiaomi, true);
       // boolean is2 = isMotionImage(google, true);

        Map<String, Object> metadata = metadata(xiaomi);
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        System.out.println(gson.toJson(metadata));

    }

    public static Map<String,Object> metadata(String fileOrUriPath){
        boolean isMotion = isMotionImage(fileOrUriPath,true);
        Map<String, Object> map = new TreeMap<>();
        map.put("0-path",fileOrUriPath);
        if(fileOrUriPath !=null && !fileOrUriPath.equals("")){
            File file = new File(fileOrUriPath);
            map.put("0-length",file.length());
            map.put("0-lastModified",file.lastModified());
        }

        Map<String, Object> stringObjectMap = motion.metaOfImage(fileOrUriPath);
        map.put("image",stringObjectMap);
        if(isMotion){
            String path = getMotionVideoPath(fileOrUriPath);
            Map<String, Object> stringObjectMap1 = motion.metaOfVideo(path);
            map.put("video",stringObjectMap1);
        }
        return map;

    }


    public static boolean isMotionImage(String fileOrUriPath, boolean extractVideo) {
        if (fileOrUriPath == null || fileOrUriPath.equals("")) {
            return false;
        }
        String videoPath = motion.mp4CacheFile(fileOrUriPath);
        File video = new File(videoPath);
        if (video.exists() && video.length() > 500) {
            System.out.println("video 文件已经存在: " + videoPath);
            return true;
        }
        try {
            long wholeFileLength = motion.length(fileOrUriPath);
            String xmp = motion.readXmp(fileOrUriPath);
            if (xmp == null || xmp.equals("")) {
                return false;
            }
            String androidXmp = "xmlns:GCamera=";
            String iosXmp = "iosxxx";
            if (!xmp.contains(androidXmp) && !xmp.contains(iosXmp)) {
                return false;
            }
            if (xmp.contains(androidXmp)) {
                    String regex0 = "GCamera:MicroVideoOffset=\"(\\d+)\"";
                    // 创建 Pattern 对象
                    Pattern pattern0 = Pattern.compile(regex0);
                    // 创建 matcher 对象
                    Matcher matcher0 = pattern0.matcher(xmp);
                    // 查找并提取数字
                    if (matcher0.find()) {
                        String number = matcher0.group(1);
                        int length = Integer.parseInt(number);
                        System.out.println("提取到的数字是: " + number);
                        if (wholeFileLength <= length) {
                            System.out.println("文件大小小于视频文件大小, xmp显示是动态图,但实际不是 " + fileOrUriPath);
                            return false;
                        }
                        //提取视频文件
                        if (extractVideo) {
                            extractMp4FromMotionPhoto(fileOrUriPath, motion.mp4CacheFile(fileOrUriPath), wholeFileLength - length, length);
                        }

                        return true;
                    } else {
                        String regex = "<Container:Item\\s+Item:Mime=\"video/mp4\"\\s+Item:Semantic=\"MotionPhoto\"\\s+Item:Length=\"(\\d+)\"\\s+Item:Padding=\"(\\d+)\"\\s*/>";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(xmp);
                        if (matcher.find()) {
                            String length = matcher.group(1);
                            String padding = matcher.group(2);
                            System.out.println("length: " + length + ", padding:" + padding);
                            int len = Integer.parseInt(length);
                            int pad = Integer.parseInt(padding);
                            if (wholeFileLength <= len + pad) {
                                System.out.println("文件大小小于视频文件大小2, xmp显示是动态图,但实际不是 " + fileOrUriPath);
                                return false;
                            }
                            //提取视频文件
                            if (extractVideo) {
                                extractMp4FromMotionPhoto(fileOrUriPath, motion.mp4CacheFile(fileOrUriPath), wholeFileLength - (len + pad), len);
                            }
                            return true;

                        } else {
                           // System.out.println("没有找到匹配的模式。");
                        }

                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    public static String getMotionVideoPath(String filePath) {
        boolean isMotionImage = isMotionImage(filePath, true);
        if (!isMotionImage) {
            return null;
        }
        return motion.mp4CacheFile(filePath);
    }


    static void extractMp4FromMotionPhoto(String inputFile, String outputFile, long startBytes, long length) throws IOException {
        RandomAccessFile raf = null;
        RandomAccessFile rafOutput = null;
        try {
            // 打开输出 MP4 文件
            File out = new File(outputFile);
            if (!out.exists()) {
                out.createNewFile();
            } else {
                if (length == out.length()) {
                    return;
                }
                out.delete();
                out.createNewFile();
            }
            // 打开 Motion Photo 文件
            raf = new RandomAccessFile(new File(inputFile), "r");
            rafOutput = new RandomAccessFile(out, "rw");

            // 定位到指定字节位置开始读取
            raf.seek(startBytes);

            // 每次读取的缓冲区大小
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = raf.read(buffer)) != -1) {
                rafOutput.write(buffer, 0, bytesRead);
            }
        } finally {
            if (raf != null) {
                raf.close();
            }
            if (rafOutput != null) {
                rafOutput.close();
            }
        }
    }
}
