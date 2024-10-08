package com.hss01248.motion_photos;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<String, Object> metaOfImage(String fileOrUriPath) {
        File file = new File(fileOrUriPath);
        if(!file.exists() ){
            return null;
        }
        if(!file.isFile()){
            return null;
        }
        if(file.length() ==0){
            return null;
        }
        String cmdarray[] = new String[]{"exiftool",fileOrUriPath,"-j"};
        try {
            Process exec = Runtime.getRuntime().exec(cmdarray);
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
            reader.close();

            // 将输出转换为字符串
            String result = output.toString();
            Gson gson = new Gson();
            List<Map> map2 = gson.fromJson(result, new TypeToken<List<Map>>(){}.getType());
            //System.out.println(result);
            return map2.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> metaOfVideo(String fileOrUriPath) {
        return metaOfImage(fileOrUriPath);
    }
}
