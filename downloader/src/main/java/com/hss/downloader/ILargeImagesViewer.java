package com.hss.downloader;

import android.content.Context;

import java.util.List;

public interface ILargeImagesViewer {

   void  showBig(final Context context,  final List<String> uris0, int position);

   void viewDir(Context context,String dir,String file);
}
