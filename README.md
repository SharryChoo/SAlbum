## SAlbum
SAlbum 是一款对 Android 端提供 **图片的选取、裁剪、拍摄和短视频录制等功能的图库框架**

## 功能介绍
- **图片的选取**
  - 支持 JPEG/PNG/WEBP/GIF 的选取
  - 图片加载引擎由用户自定义实现
- **图片的浏览**
  - 共享元素跳转动画
- **图像的裁剪**
- **相机的拍摄**
  - ~~CameraX-alpha4 尺寸选取存在问题, 暂时移除~~
  - 提供 1:1、4:3、16:9 的比例选择
     - 支持 CenterCrop 全屏预览
  - 通过自定义 Renderer, 可拓展水印滤镜等效果
- **视频的录制**
  - 视频
    - 使用 MediaCodec 实现 H.264 的硬编
    - 支持 1080p, 720p, 480p 的录制分辨率
  - 音频
    - PCM 数据获取使用 OpenSL ES, 支持 v7a
    - 使用 MediaCodec 硬编为 AAC
  - 使用 MediaMuxer 合并为 mp4 文件
- **视频的播放**
  - 考虑到依赖体积, 使用系统提供的 VideoView 实现

## 功能集成
[![](https://jitpack.io/v/SharryChoo/SAlbum.svg)](https://jitpack.io/#SharryChoo/SAlbum)

### Step 1
Add it in your **module build.gradle** at the end of repositories
```
dependencies {
    ...
    // SAlbum dependency
    implementation 'com.github.SharryChoo:SAlbum:+'
     
    // Need Android dependencies
    implementation "androidx.constraintlayout:constraintlayout:+"
    implementation "androidx.appcompat:appcompat:+"
    implementation "androidx.recyclerview:recyclerview:+"
    implementation "com.google.android.material:material:+"
}
```

### Step 2
Add it in your **root build.gradle** at the end of repositories
```
allprojects {
    repositories {
	    ...
	    maven { url 'https://jitpack.io' }
    }
}
```

## 效果展示
### 资源选取
![资源选取](https://raw.githubusercontent.com/SharryChoo/SAlbum/release/assert/PicturePicker.jpg)

### 图像拍摄
![图像拍摄](https://raw.githubusercontent.com/SharryChoo/SAlbum/release/assert/PictureTaker.png)

### 视频录制
![视频录制](https://raw.githubusercontent.com/SharryChoo/SAlbum/release/assert/VideoRecord.png)

### 视频播放
![视频的播放](https://raw.githubusercontent.com/SharryChoo/SAlbum/release/assert/VideoPlay.jpg)

## 功能使用
SPicturePicker 的所有功能提供, 均通过 **Manager** 对外提供, 其具体的功能选项通过 **Config** 来配置

功能 | Manager | Config
:---:|:---:|:---:
选取 | PickerManager | PickerConfig
浏览 | WatcherManager | WatcherConfig
拍摄/录像 | TakerManager | TakerConfig
裁剪 | CropperManager | CropperConfig

### 选取
```
PickerManager.with(context)
        // 设置选择配置文件
        .setPickerConfig(
                PickerConfig.Builder()
                        // 阈值
                        .setThreshold(etAlbumThreshold.text.toString().toInt())
                        // 每行展示的数量
                        .setSpanCount(etSpanCount.text.toString().toInt())
                        // 是否开启 Toolbar Behavior 动画
                        .isToolbarScrollable(cbAnimation.isChecked)
                        // 是否开启 Fab Behavior 动画
                        .isFabScrollable(cbAnimation.isChecked)
                        // 是否选择 GIF 图
                        .isPickGif(cbGif.isChecked)
                        // 是否选择视频
                        .isPickVideo(cbVideo.isChecked)
                        // Toolbar 背景设置
                        .setToolbarBackgroundColor(
                                ContextCompat.getColor(this, R.color.colorPrimary)
                        )
                        // 指示器填充色
                        .setIndicatorSolidColor(
                                ContextCompat.getColor(this, R.color.colorPrimary)
                        )
                        // 选中指示器的颜色
                        .setIndicatorBorderColor(
                                ContextCompat.getColor(this, R.color.colorPrimary),
                                ContextCompat.getColor(this, android.R.color.white)
                        )
                        // 指示器边界的颜色
                        .setPickerItemBackgroundColor(
                                ContextCompat.getColor(this, android.R.color.white)
                        )
                        // 注入用户已选中的图片集合
                        .setUserPickedSet(mPickedSet)
                        // 设置相机配置, 非 null 说明支持相机(拍摄/录制)
                        .setCameraConfig(
                                if (cbCamera.isChecked) takerConfig else null
                        )
                        // 设置裁剪配置, 非 null 说明支持裁剪
                        .setCropConfig(
                                if (cbCrop.isChecked) cropperConfig else null
                        )
                        .build()
        )
        // 加载框架注入
        .setLoaderEngine(
                object : ILoaderEngine {
                    /**
                     * 加载图片
                     */
                    override fun loadPicture(context: Context, uri: String, imageView: ImageView) {
                        // 保证为静态图
                        Glide.with(context).asBitmap().load(uri).into(imageView)
                    }
                    /**
                     * 加载动态图
                     */
                    override fun loadGif(context: Context, uri: String, imageView: ImageView) {
                        // 保证为 GIF 图
                        Glide.with(context).asGif().load(uri).into(imageView)
                    }
                    /**
                     * 加载视频缩略图
                     */
                    override fun loadVideoThumbnails(context: Context, uri: String, thumbnailPath: String?, imageView: ImageView) {
                        // Glide 可直接加载视频 uri 获取第一帧
                        Glide.with(context).asBitmap().load(uri).into(imageView)
                    }
                }
        )
        .start {
            // TODO 选中的资源, 通过 ArrayList<MediaMeta> 返回
        }
```
选取的方式如上所示, **首先按照需求构建 Config**, **然后注入图片加载的引擎**, 之后便可以在 start 的回调中获取到选中的图片资源了
- 关于相机
  - 在 PickerConfig 中传入相机的配置, 则意为开启相机的功能
- 关于裁剪
  - 在 PickerConfig 中传入裁剪的配置, 则意为开启裁剪的功能

### 浏览
浏览的功能与选取类似, 打开图片选择器时, 会根据 PickerConfig 自动生成浏览的配置, 若想在外界单独使用图片浏览的功能, 可以通过以下方式
```
WatcherManager.with(context)
        .setConfig(
            WatcherConfig.Builder()
                // 配置 Indicator 的展示效果
                .setIndicatorTextColor(mPickerConfig.getIndicatorTextColor())
                .setIndicatorSolidColor(mPickerConfig.getIndicatorSolidColor())
                .setIndicatorBorderColor(
                        mPickerConfig.getIndicatorBorderCheckedColor(),
                        mPickerConfig.getIndicatorBorderUncheckedColor()
                )
                // 注入需要展示的图片
                .setDisplayDataSet(mPickedSet, 0)
                // 设置最大选中数量, 若 > 0, 则说明图片查看器也支持选取的功能
                .setThreshold(mPickerConfig.getThreshold())
                // 注入用户选中的图片集合
                .setUserPickedSet(mPickedSet)
                .build();
        )
        // 注入共享元素
        .setSharedElement(sharedElement)
        // 注入图片加载器
        .setLoaderEngine(Loader.getPictureLoader())
        .startForResult(this);
```
可以看到浏览的使用主要区别在于, 增加了
**共享元素(支持 5.0 以下的操作系统)**
的选项
- 当 threshold > 0 时, 表示需要为图片浏览添加图片选择功能, 反之仅做图片查看使用


### 拍摄
相机的使用与浏览类似, 可以集成在 Picker 中使用, 也可以单独使用
```
TakerManager.with(context)
        .setConfig(
            TakerConfig.Builder()
                // 预览画面比例, 支持 1:1, 4:3, 16:9
                .setPreviewAspect(ASPECT_1_1)
                // 是否全屏预览(在比例基础上进行 CenterCrop, 保证画面不畸形)
                .setFullScreen(false)
                // 设置自定义 Renderer 的路径
                .setRenderer(WatermarkPreviewerRenderer::class.java)
                // 设置是否支持视频录制
                .setVideoRecord(true)
                // 设置录制最大时长
                .setMaxRecordDuration(15 * 1000)
                // 设置录制最短时长
                .setMinRecordDuration(1 * 1000)
                // 设置录制的分辨率
                .setRecordResolution(Options.Video.RESOLUTION_720P)
                // 设置文件存储路径
                .setDirectoryPath(APP_DIRECTORY)
                // 拍摄后质量压缩
                .setPictureQuality(80)
                // 注入裁剪配置, 非 null, 表示拍摄之后进行图片的裁剪
                .setCropConfig(...)
                .build()
        )
        .take(this);
```
其中的注释比较清晰, 操作完成之后, 可通过回调获取到拍摄/录制的结果

**关于自定义 Camera 的渲染器, 需要用户自定义实现 IPreviewer.Renderer 这个接口**, Demo 中提供了一个水印效果的渲染器滤镜, 可以其参考实现自己的渲染器
```
public Builder setRenderer(@NonNull Class<? extends IPreviewer.Renderer> rendererClass) {
    try {
        rendererClass.getDeclaredConstructor(Context.class);
    } catch (NoSuchMethodException e) {
        throw new UnsupportedOperationException("Please ensure " + rendererClass.getSimpleName()
                + " have a constructor like: " + rendererClass.getSimpleName() + "(Context context)");
    }
    mConfig.rendererClsName = rendererClass.getName();
    return this;
}
```
传入渲染器实现的 class 文件, 需要保证提供一个参数为 Context 的构造方法, 否则在构建 TakerConfig 时会出现异常

### 裁剪
```
CropperManager.with(context)
        .setConfig(
            CropperConfig.Builder()
                // 要裁剪的图片路径
                .setOriginFile(...)
                // 指定 FileProvider 的 authority, 用于 7.0 获取文件 URI
                .setFileProviderAuthority("$packageName.FileProvider")
                // 裁剪后文件输出的路径
                .setCropDirectory(APP_DIRECTORY)
                // 裁剪期望的尺寸
                .setCropSize(1000, 1000)
                // 裁剪后的质量
                .setCropQuality(80)
                .build()
        )
        .crop(this);
```
裁剪目前使用系统提供的裁剪方式, 需要获取文件的 URI, 7.0 之后获取 URI 需要通过 FileProvider, 因此这里需要传入 FileProvider 的 authority, 关于这一块网上的资料比较多, 这里就不再赘述了

## 其他
更多功能请查看工程中提供的示例