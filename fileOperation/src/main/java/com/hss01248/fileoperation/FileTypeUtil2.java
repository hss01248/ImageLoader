package com.hss01248.fileoperation;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileTypeUtil2 {


    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_DOC_PDF = "pdf";//pdf
    public static final String TYPE_DOC_WORD = "word";//msword
    public static final String TYPE_DOC_EXCEL = "excel";//excel
    public static final String TYPE_DOC_PPT = "ppt";//powerpoint
    public static final String TYPE_DOC_TXT = "txt";  //文件名.txt

    //思维导图
    public static final String TYPE_DOC_mind = "mind";
    public static final String TYPE_stream_zip = "zip";

    public static final String TYPE_DOC_markdown = "markdown";

    public static final String TYPE_UNKNOWN = "unknown";

    public static final int INT_TYPE_IMAGE = 1;
    public static final int INT_TYPE_VIDEO = 2;
    public static final int INT_TYPE_AUDIO = 3;
    public static final int INT_TYPE_DOC_PDF = 4;//pdf
    public static final int INT_TYPE_DOC_WORD = 5;//msword
    public static final int INT_TYPE_DOC_EXCEL = 6;//excel
    public static final int INT_TYPE_DOC_PPT = 7;//powerpoint
    public static final int INT_TYPE_DOC_TXT = 8;  //文件名.txt


    //思维导图
    public static final int INT_TYPE_DOC_mind = 9;
    public static final int INT_TYPE_stream_zip = 10;

    public static final int INT_TYPE_DOC_markdown = 11;

    public static final int INT_TYPE_UNKNOWN = -1;

    /**
     * 图片: https://zh.wikipedia.org/wiki/%E5%9B%BE%E5%BD%A2%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F%E6%AF%94%E8%BE%83
     * 视频后缀
     * 最常见：.mpg .mpeg .avi .rm .rmvb .mov .wmv .asf .dat
     * 不常见的：.asx .wvx .mpe .mpa
     * 音频后缀
     * 常见的：.mp3 .wma .rm .wav .mid
     * .ape .flac
     * <p>
     * 常见 MIME 类型列表
     * https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
     * <p>
     * 作者：耐住寂寞守住繁华_5b9a
     * 链接：https://www.jianshu.com/p/8962f2a5186e
     * 来源：简书
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     *
     * @param name
     * @return
     */


    static Map<String, List<String>> mimeMap = new HashMap<>();
    static Map<String, String> mimeMap2 = new HashMap<>();

    static Map<String, Integer> mimeMap3 = new HashMap<>();


    static {
        mimeMap.put(TYPE_DOC_PPT,getTypeExtension(TYPE_DOC_PPT));
        mimeMap.put(TYPE_AUDIO,getTypeExtension(TYPE_AUDIO));
        mimeMap.put(TYPE_DOC_EXCEL,getTypeExtension(TYPE_DOC_EXCEL));
        mimeMap.put(TYPE_DOC_PDF,getTypeExtension(TYPE_DOC_PDF));
        mimeMap.put(TYPE_DOC_TXT,getTypeExtension(TYPE_DOC_TXT));
        mimeMap.put(TYPE_DOC_WORD,getTypeExtension(TYPE_DOC_WORD));
        mimeMap.put(TYPE_VIDEO,getTypeExtension(TYPE_VIDEO));
        mimeMap.put(TYPE_IMAGE,getTypeExtension(TYPE_IMAGE));
        mimeMap.put(TYPE_stream_zip,getTypeExtension(TYPE_stream_zip));
        mimeMap.put(TYPE_DOC_mind,getTypeExtension(TYPE_DOC_mind));
        mimeMap.put(TYPE_DOC_markdown,getTypeExtension(TYPE_DOC_markdown));
        initMimeMap2();
        initMimeMap3();
    }

    private static void initMimeMap3() {
        mimeMap3.put(TYPE_DOC_PPT,INT_TYPE_DOC_PPT);
        mimeMap3.put(TYPE_DOC_WORD,INT_TYPE_DOC_WORD);
        mimeMap3.put(TYPE_DOC_EXCEL,INT_TYPE_DOC_EXCEL);
        mimeMap3.put(TYPE_DOC_markdown,INT_TYPE_DOC_markdown);
        mimeMap3.put(TYPE_AUDIO,INT_TYPE_AUDIO);
        mimeMap3.put(TYPE_DOC_PDF,INT_TYPE_DOC_PDF);
        mimeMap3.put(TYPE_IMAGE,INT_TYPE_IMAGE);
        mimeMap3.put(TYPE_VIDEO,INT_TYPE_VIDEO);
        mimeMap3.put(TYPE_DOC_mind,INT_TYPE_DOC_mind);
        mimeMap3.put(TYPE_DOC_TXT,INT_TYPE_DOC_TXT);
        mimeMap3.put(TYPE_stream_zip,INT_TYPE_stream_zip);

    }

    private static void initMimeMap2() {
        for (Map.Entry<String, List<String>> entry : mimeMap.entrySet()) {
            List<String> value = entry.getValue();
            for (String s : value) {
                mimeMap2.put(s,entry.getKey());
            }
        }

    }

    public static  boolean canScanToDB(String name){
        if(name.endsWith(".3")){
            name = name.substring(0,name.length()-2);
        }
        final int lastDot = name.lastIndexOf('.');
        if (lastDot < 0) {
            return false;
        }
        final String extension = name.substring(lastDot + 1).toLowerCase();
        return mimeMap2.containsKey(extension);
    }

    private static List<String> getTypeExtension(String type){
        List<String> ext = new ArrayList<>();
        switch (type){
            case TYPE_DOC_PPT:
                ext.add( "ppt");
                ext.add("pptx");
                break;
            case TYPE_AUDIO:
                ext.add( "mp3");
                ext.add("aac");
                ext.add( "wav");
                ext.add("m4a");
                ext.add("flac");
                ext.add("wma");
                ext.add("qmc3");
                ext.add("qmcflac");
                ext.add("ncm");
                ext.add("xm");
                ext.add("kgm");
                //qq音乐格式-2023
                ext.add("ogg");
                ext.add("mgg0");
                ext.add("mgg1");
                ext.add("mgg2");
                break;
            case TYPE_DOC_EXCEL:
                ext.add( "xls");
                ext.add("csv");
                ext.add( "xlsx");
                break;
            case TYPE_DOC_PDF:
                ext.add( "pdf");
                ext.add( "epub");
                ext.add( "mobi");
                break;
            case TYPE_DOC_TXT:
                ext.add( "txt");
                break;
            case TYPE_VIDEO:
                ext.add( "mp4");
                ext.add("mkv");
                ext.add( "avi");
                ext.add("mpeg");
                ext.add("mpg");
                ext.add("vob");
                ext.add( "wmv");
                ext.add( "asf");
                ext.add("rmvb");
                ext.add("rm");
                ext.add( "mov");
                ext.add( "vob");
                ext.add("flv");
                //ext.add("ts");
                //vdat格式视频是从网页上缓存下来的视频格式，是一种特定的文件格式，只是一种数据文件，
                // 主要用来标记视讯文件。例如我们使用uc手机浏览器缓存的视频文件就是 vdat格式的，然而一般的视频播放器是无法播放的
                ext.add("vdat");
                //对应同目录下还有一个叫.m3u8_contents的文件夹,内部是实际的分片文件. 最好是用ffmpeg转换为mp4
                //ffmpeg -i "https://相对地址/shi.m3u8" -vcodec copy -acodec copy -absf aac_adtstoasc test.mp4
                //ext.add("m3u8");
                break;
            case TYPE_DOC_WORD:
                ext.add( "doc");
                ext.add("docx");
                break;
            case TYPE_DOC_mind:
                ext.add( "xmind");
                break;
            case TYPE_stream_zip:
                ext.add( "zip");
                ext.add( "rar");
                ext.add( "gz");
                ext.add( "tar");
                break;
            case TYPE_DOC_markdown:
                ext.add( "md");
                break;
            case TYPE_IMAGE:
                ext.add( "jpg");
                ext.add("jpeg");
                ext.add( "webp");
                ext.add( "gif");
                ext.add( "png");
                ext.add("avif");
                ext.add( "heif");
                ext.add( "svg");
                break;
        }
        return ext;
    }


    public static int getTypeIntByFileName(String name){
        String type = getTypeByFileName(name);
        if(!mimeMap3.containsKey(type)){
            return INT_TYPE_UNKNOWN;
        }
        return mimeMap3.get(type);
    }

    public static boolean isImage(String name){
        return getTypeIntByFileName(name) == INT_TYPE_IMAGE;
    }

    public static boolean isVideo(String name){
        return getTypeIntByFileName(name) == INT_TYPE_VIDEO;
    }

    public static boolean isImageOrVideo(String name){
        int type = getTypeIntByFileName(name);
        return  type == INT_TYPE_IMAGE || type == INT_TYPE_VIDEO;
    }
    public static String getTypeByFileName(String name){
        if(TextUtils.isEmpty(name)){
            return TYPE_UNKNOWN;
        }

        if(name.startsWith("http")){
            if(name.contains("?")){
                name = name.substring(0,name.indexOf("?"));
            }
        }
        if(name.endsWith(".3")){
            name = name.substring(0,name.length()-2);
        }
        final int lastDot = name.lastIndexOf('.');
        if (lastDot < 0) {
            return TYPE_UNKNOWN;
        }
        final String extension = name.substring(lastDot + 1).toLowerCase();
        String s = mimeMap2.get(extension);
        if(s !=null){
            return s;
        }
        return TYPE_UNKNOWN;
    }



}
