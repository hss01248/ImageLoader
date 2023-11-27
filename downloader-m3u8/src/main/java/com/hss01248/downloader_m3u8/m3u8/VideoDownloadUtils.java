package com.hss01248.downloader_m3u8.m3u8;

import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;

import java.io.Closeable;
import java.security.MessageDigest;
import java.text.DecimalFormat;

public class VideoDownloadUtils {
    public static final long DEFAULT_CONTENT_LENGTH = -1;
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    public static final String VIDEO_SUFFIX = ".video";
    public static final String LOCAL_M3U8 = "local.m3u8";
    public static final String LOCAL_M3U8_WITH_KEY = "local_key_url.m3u8";
    public static final String REMOTE_M3U8 = "remote.m3u8";
    public static final String OUTPUT_VIDEO = "merged.mp4";
    public static final String SEGMENT_PREFIX = "video_";
    public static final String INIT_SEGMENT_PREFIX = "init_video_";
    public static final String INFO_FILE = "range.info";

    private static final Object sInfoFileLock = new Object();



    public static String computeMD5(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digestBytes = messageDigest.digest(string.getBytes());
            return bytesToHexString(digestBytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                LogUtils.w(DownloadConstants.TAG, "VideoProxyCacheUtils close " + closeable + " failed, exception = " + e);
            }
        }
    }

    public static String getPercent(float percent) {
        DecimalFormat format = new DecimalFormat("###.00");
        return format.format(percent) + "%";
    }

    public static boolean isFloatEqual(float f1, float f2) {
        if (Math.abs(f1 - f2) < 0.01f) {
            return true;
        }
        return false;
    }

    public static String getSuffixName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex >= 0 && dotIndex < name.length()) ? name.substring(dotIndex) : "";
    }



}
