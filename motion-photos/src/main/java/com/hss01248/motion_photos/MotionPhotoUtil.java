package com.hss01248.motion_photos;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MotionPhotoUtil {

    public static void setMotion(IMotion motion) {
        MotionPhotoUtil.motion = motion;
    }

    static IMotion motion = new JavaMotion();

  public   static final String xmpOfGoogle = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 5.1.0-jc003\">\n" +
            "  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
            "    <rdf:Description rdf:about=\"\"\n" +
            "        xmlns:GCamera=\"http://ns.google.com/photos/1.0/camera/\"\n" +
            "      GCamera:MicroVideo=\"1\"\n" +
            "      GCamera:MicroVideoVersion=\"1\"\n" +
            "      GCamera:MicroVideoOffset=\"xxxx\"\n" +
            "      GCamera:MicroVideoPresentationTimestampUs=\"231070\"/>\n" +
            "  </rdf:RDF>\n" +
            "</x:xmpmeta>";

    public static void main(String[] args) {
        String xiaomi = "/Users/hss/Documents/MVIMG_20240918_093751.jpg";
        String google = "/Users/hss/Documents/PXL_20240918_013738178.MP.jpg";
        boolean isMotionImage = isMotionImage(xiaomi,true);
        boolean is2 = isMotionImage(google,true);
        System.out.println("file type box: xiaomi  "+getFileTypeBox(xiaomi));
        System.out.println("file type box: google "+getFileTypeBox(google));

        String jpg = "/Users/hss/Documents/码.jpg";
        System.out.println("file type box: jpg  "+getFileTypeBox(jpg));

        String jpg2 = "/Users/hss/Documents/8b7ac90254e59ae4761f99a75a604890.jpg";
        System.out.println("file type box: jpg2  "+getFileTypeBox(jpg2));

    }

    public static String getFileTypeBox(String filePath)  {
        RandomAccessFile file = null;
        try {
         file = new RandomAccessFile(filePath, "r");

            file.seek(4); // Skip the first 4 bytes (header)
            byte[] ftypBytes = new byte[4];
            if (file.read(ftypBytes) == 4) {
                String fileTypeBox = new String(ftypBytes, "ISO-8859-1");
                System.out.println(fileTypeBox+"  -> "+filePath +", ");
                return fileTypeBox;
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        finally {
            if(file !=null){
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }


    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }



    public static boolean isMotionImage(String fileOrUriPath,boolean extractVideo){
        if(fileOrUriPath ==null || fileOrUriPath.equals("")){
            return false;
        }
        String videoPath = motion.mp4CacheFile(fileOrUriPath);
        File video = new File(videoPath);
        if(video.exists() && video.length()> 500){
            System.out.println("video 文件已经存在: "+videoPath);
            return true;
        }
        try {
            long wholeFileLength = motion.length(fileOrUriPath);
            String xmp = motion.readXmp(fileOrUriPath);
            if(xmp ==null || xmp.equals("")){
                return false;
            }
            String androidXmp = "xmlns:GCamera=";
            //"http://ns.google.com/photos/1.0/camera/"
            String iosXmp = "iosxxx";
            if(!xmp.contains(androidXmp) && !xmp.contains(iosXmp)){
                return false;
            }
            if(xmp.contains(androidXmp)){
               // String xiaomiXmp = "xmlns:MiCamera=\"http://ns.xiaomi.com/photos/1.0/camera/\"";
                //if(xmp.contains(xiaomiXmp)){
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
                        if(wholeFileLength <= length){
                            System.out.println("文件大小小于视频文件大小, xmp显示是动态图,但实际不是 " + fileOrUriPath);
                            return false;
                        }
                        //提取视频文件
                        if(extractVideo){
                            extractMp4FromMotionPhoto(fileOrUriPath,motion.mp4CacheFile(fileOrUriPath),wholeFileLength -length,length);
                        }

                        return true;
                    } else {
                        System.out.println("android version 2。");
                        String regex = "<Container:Item\\s+Item:Mime=\"video/mp4\"\\s+Item:Semantic=\"MotionPhoto\"\\s+Item:Length=\"(\\d+)\"\\s+Item:Padding=\"(\\d+)\"\\s*/>";

                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(xmp);
                        if (matcher.find()) {
                            String length = matcher.group(1);
                            String padding = matcher.group(2);
                            System.out.println("length: "+length+", padding:"+padding);
                            int len = Integer.parseInt(length);
                            int pad = Integer.parseInt(padding);
                            if(wholeFileLength <= len + pad){
                                System.out.println("文件大小小于视频文件大小2, xmp显示是动态图,但实际不是 " + fileOrUriPath);
                                return false;
                            }
                            //提取视频文件
                            if(extractVideo){
                                extractMp4FromMotionPhoto(fileOrUriPath,motion.mp4CacheFile(fileOrUriPath),wholeFileLength -(len+pad),len);
                            }
                            return true;
                        }else {
                            System.out.println("xmp没有找到匹配的内容。");
                        }
                    }
            }else{

            }

        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    public static String getMotionVideoPath(String filePath){
        boolean isMotionImage = isMotionImage(filePath,true);
        if(!isMotionImage){
            return null;
        }
        return  motion.mp4CacheFile(filePath);
    }



     static void extractMp4FromMotionPhoto(String inputFile, String outputFile, long startBytes,long length) throws IOException {
        RandomAccessFile raf = null;
        RandomAccessFile rafOutput = null;
        try {
            // 打开输出 MP4 文件
            File out = new File(outputFile);
            if(!out.exists()){
                out.createNewFile();
            }else {
                if(length == out.length()){
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
