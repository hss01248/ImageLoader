package com.hss01248.fileoperation;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.Utils;

import io.reactivex.Observer;

/**
 * @Despciption 官方文档: https://developer.android.com/training/data-storage/use-cases?hl=zh-cn
 *
 * https://developer.android.com/training/data-storage/shared?hl=zh-cn
 *
 * 为新下载内容定义存储位置
 * 如果您的应用使用分区存储，请注意您选择存储下载媒体文件的位置。
 *
 * 如果其他应用需要访问文件，不妨针对下载内容或文档集合考虑使用明确定义的媒体集合。
 *
 * 注意：在 Android 11 或更高版本（无论目标 SDK 级别是什么）中，其他应用无法访问外部存储设备上的应用专用目录中存储的文件。
 * 在 Android 11 及更高版本中，即使您使用 DownloadManager 提取文件，其他应用也无法访问外部应用专用目录中的这些文件。
 *
 *
 * 将用户媒体文件导出到设备
 * 定义适当的默认位置来存储用户媒体文件：
 *
 * 允许用户使用应用专用存储空间或共享存储空间，选择是否允许其他应用读取其媒体文件。
 * 允许用户将文件从应用专用目录导出到一个更常用的位置。使用 MediaStore 的图片、视频和音频集合将媒体文件导出到设备的媒体库中。
 * 注意：为避免混乱，请使用 externalStoragePublicDirectory() 或 externalMediaDirs() 等通常可访问的位置。
 *
 *
 *      * 将非媒体文件导出到设备
 *      * 定义一个适当的默认位置来存储非媒体文件。允许用户将文件从应用专用目录导出到一个更常用的位置。使用 MediaStore 的下载内容或文档集合，可将非媒体文件导出到设备。
 *      *
 *      * 注意：为避免混乱，请使用 externalStoragePublicDirectory() 或 externalMediaDirs() 等通常可供访问的位置。
 *
 * @Author hss
 * @Date 23/02/2022 11:27
 * @Version 1.0
 */
public class FileAddUtil {

    /**
     * 将用户媒体文件导出到设备
     * 定义适当的默认位置来存储用户媒体文件：
     *
     * 允许用户使用应用专用存储空间或共享存储空间，选择是否允许其他应用读取其媒体文件。
     * 允许用户将文件从应用专用目录导出到一个更常用的位置。使用 MediaStore 的图片、视频和音频集合将媒体文件导出到设备的媒体库中。
     * 注意：为避免混乱，请使用 externalStoragePublicDirectory() 或 externalMediaDirs() 等通常可访问的位置。
     * @param bitmap
     * @param callback
     */
    public static void saveToAlbum(Bitmap bitmap, Observer<String> callback){


    }

    /**
     * 将非媒体文件导出到设备
     * 定义一个适当的默认位置来存储非媒体文件。允许用户将文件从应用专用目录导出到一个更常用的位置。使用 MediaStore 的下载内容或文档集合，可将非媒体文件导出到设备。
     *
     * 注意：为避免混乱，请使用 externalStoragePublicDirectory() 或 externalMediaDirs() 等通常可供访问的位置。
     * @param path
     */
    public static void extraNotMediaFileToPublicDir(String path){

    }
}
