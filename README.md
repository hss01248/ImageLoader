# ImageLoader

图片加载框架的api封装

api设计参考glide,目前底层依赖fresco

# 初始化

Application中:

传入全局context和定义缓存文件夹的大小

```
init(final Context context, int cacheSizeInM)
```

# 入口方法

> 该方法返回SingleConfig.ConfigBuilder

```
ImageLoader.with(this)
```

# 自定义设置

## 几种不同的图片源

```
.url(String url)

.file(String filePath)

.res(int resId)

.content(String contentProvider)
```

## https时是否忽略证书校验

> 默认不忽略

```
.ignoreCertificateVerify(boolean ignoreCertificateVerify)
```

## 传入宽高,用于resize,以节省内存

> 一般,传如用于显示的那个view的宽高就行,单位为dp.内部已自行转换为px
>
> 框架能根据这两个参数去把图片流解压成这个大小的bitmap,可以节约内存空间

```
widthHeight(int width,int height)
```

## 占位图/默认图

```
placeHolder(int placeHolderResId)
```

## 圆角矩形

> 可以设置圆角的半径,以及当图片是gif动图时,用什么颜色来盖一层,以实现动图时的圆角

```
rectRoundCorner(int rectRoundRadius,int overlayColorWhenGif)
```

## 设置图片为圆形

```
asCircle(int overlayColorWhenGif)
```

## 图片的边框

```
border(int borderWidth,int borderColor)
```

## 缩放模式

> 参考fresco的模式设置,取值为ScaleMode.xxx.
>
> 这个模式只会改变bitmap展示在view上面的模式,而不会改变bitmap在内存中的大小

```
scale(int scaleMode)
```

| 类型                                       | 描述                                       |
| ---------------------------------------- | ---------------------------------------- |
| center                                   | 居中，无缩放。                                  |
| centerCrop                               | 保持宽高比缩小或放大，使得两边都大于或等于显示边界，且宽或高契合显示边界。居中显示。 |
| [focusCrop](https://www.fresco-cn.org/docs/scaling.html#focusCrop) | 同centerCrop, 但居中点不是中点，而是指定的某个点。          |
| centerInside                             | 缩放图片使两边都在显示边界内，居中显示。和 `fitCenter` 不同，不会对图片进行放大。如果图尺寸大于显示边界，则保持长宽比缩小图片。 |
| fitCenter                                | 保持宽高比，缩小或者放大，使得图片完全显示在显示边界内，且宽或高契合显示边界。居中显示。 |
| fitStart                                 | 同上。但不居中，和显示边界左上对齐。                       |
| fitEnd                                   | 同fitCenter， 但不居中，和显示边界右下对齐。              |
| fitXY                                    | 不保存宽高比，填充满显示边界。                          |

## 配置高斯模糊

> 传入的参数blurRadius为模糊度,越大就越模糊,实际效果要自己调试好相应的数字

```
blur(int blurRadius)
```

# 最终的出口方法有两个

## 显示到某一个view中

```
into(View targetView)
```

## 或者,只拿bitmap引用

> 此时,scale的配置是无效的,因为没有view去给它展示

```
asBitmap(BitmapListener bitmapListener)
```



# 示例代码:

```
ImageLoader.with(this)
        .url("https://pic1.zhimg.com/v2-7868c606d6ddddbdd56f0872e514925c_b.jpg")
        .placeHolder(R.mipmap.ic_launcher)
        .widthHeight(250,150)
        .asCircle(R.color.colorPrimary)
        .into(ivUrl);
        
ImageLoader.with(this)
                .placeHolder(R.mipmap.ic_launcher)
                .res(R.drawable.thegif)
                .widthHeight(250,150)
                .rectRoundCorner(15,R.color.colorPrimary)
                .asBitmap(new SingleConfig.BitmapListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        Log.e("bitmap",bitmap.getWidth()+"---height:"+bitmap.getHeight()+"--"+bitmap.toString());
                    }

                    @Override
                    public void onFail() {
                        Log.e("bitmap","fail");

                    }
                });
```
# usage

## gradle

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
    allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }

```

**Step 2.** Add the dependency

```
    dependencies {
            compile 'com.github.hss01248:ImageLoader:0.0.1'
    }
```