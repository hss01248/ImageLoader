package com.hss01248.imagedebugger;

public abstract class IImageSource {


   public abstract String getUri();

    public abstract String getErrorDes();

    public abstract  void getLocalFilePath(IImgLocalPathGetter getter);

    public  long getCost(){
        return 0;
    }
}
