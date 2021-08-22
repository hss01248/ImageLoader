package com.hss01248.webviewspider.spider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListToDetailImgsInfo {

    public String listTitle;
    public String listUrl;
    public String saveFolderName;

    public List<String> detailUrls = new ArrayList<>();
    public List<String> detailTitles = new ArrayList<>();

    public List<String> imagUrls = new ArrayList<>();

    public Map<String,List<String>> titlesToImags = new HashMap<>();


    //下载保存参数
    public String saveDirPath;
    public boolean hiddenFolder;

}
