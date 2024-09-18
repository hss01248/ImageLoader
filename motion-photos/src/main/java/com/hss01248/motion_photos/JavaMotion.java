package com.hss01248.motion_photos;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @Despciption todo
 * @Author hss
 * @Date 9/18/24 10:13 AM
 * @Version 1.0
 */
public class JavaMotion implements IMotion{
    @Override
    public long length(String fileOrUriPath) throws Throwable {
        return new File(fileOrUriPath).length();
    }

    @Override
    public String readXmp(String filePath)  throws Throwable{
        File file = new File(filePath);
        if(!file.exists() ){
            return null;
        }
        if(!file.isFile()){
            return null;
        }
        if(file.length() ==0){
            return null;
        }
        String cmdarray[] = new String[]{"exiftool",filePath,"-xmp","-b"};
        try {
            Process exec = Runtime.getRuntime().exec(cmdarray);
            OutputStream outputStream = exec.getOutputStream();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
            reader.close();

            // 将输出转换为字符串
            String result = output.toString();
            System.out.println(result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String mp4CacheFile(String path) {
        return path+".mp4";
    }
}
