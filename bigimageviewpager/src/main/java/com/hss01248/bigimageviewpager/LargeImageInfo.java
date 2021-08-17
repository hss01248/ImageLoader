package com.hss01248.bigimageviewpager;

import android.text.TextUtils;

import androidx.annotation.Keep;

import com.hss01248.media.metadata.ExifUtil;

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
        if(!TextUtils.isEmpty(localPathOrUri)){
           str +=  ExifUtil.getExifStr(localPathOrUri);
        }
        return str;
    }

}
