package com.hss01248.motion_photos;

/**
 * @Despciption todo
 * @Author hss
 * @Date 9/18/24 9:33 AM
 * @Version 1.0
 */
public interface IMotion {

    long length(String fileOrUriPath) throws Throwable;

    String readXmp(String fileOrUriPath) throws Throwable;

    String mp4CacheFile(String fileOrUriPath);
}
