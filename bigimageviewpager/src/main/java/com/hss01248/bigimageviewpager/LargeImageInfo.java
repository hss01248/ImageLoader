package com.hss01248.bigimageviewpager;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

import com.hss01248.media.metadata.ExifUtil;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

@Keep
public class LargeImageInfo {

    public String uri;
    public String localPathOrUri;
    public Throwable throwable;


    public String getInfo(){
        Map map = new TreeMap();
        map.put("000-uri",uri);
        if(throwable != null){
            map.put("00-exception",throwable.getClass()+" "+throwable.getMessage());
        }
        String str = map.toString().replace(",","\n");
        str = str +"\n";
        if(!TextUtils.isEmpty(localPathOrUri)){
           str +=  ExifUtil.getExifStr(localPathOrUri);
        }
        return str;
    }
/* public boolean saveTo(String dirPath){
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        if(!uri.startsWith("http")){
            Log.d("img","uri not http");
            return false;
        }
        if(TextUtils.isEmpty(localPathOrUri)){
            Log.d("img","localPathOrUri is null");
            return false;
        }

    }*/


}
