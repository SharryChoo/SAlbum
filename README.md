# Current Version 
[![](https://jitpack.io/v/FrankChoo/PicturePicker.svg)](https://jitpack.io/#FrankChoo/PicturePicker)

# How to integration
### Step 1
Add it in your **root build.gradle** at the end of repositories
```
allprojects {
    repositories {
	...
	maven { url 'https://jitpack.io' }
    }
}
```

### Step 2
Add it in your **module build.gradle** at the end of repositories
```
dependencies {
    ...
    implementation 'com.github.FrankChoo:PicturePicker:1.1.+'
    implementation 'com.android.support:appcompat-v7:27.+'
    implementation 'com.android.support:design:27.+'
    implementation 'com.android.support:recyclerview-v7:27.+'
}
```

# Preview([图片无法显示](http://note.youdao.com/noteshare?id=ee9a0d7909afc4e66b6dda57df10eda6&sub=125F838B572242DBA6B85FE66D89F77C))
- 图片裁剪
  - ![图片裁剪.gif](https://user-gold-cdn.xitu.io/2018/8/6/1650cff2ccf5f4fa?w=282&h=500&f=gif&s=4452628)

- 权限与拍照
  - ![权限与拍照.gif](https://user-gold-cdn.xitu.io/2018/8/6/1650cff2cfcacddc?w=282&h=500&f=gif&s=3251641)

- Material Design 动画
  - ![Material Design 动画.gif](https://user-gold-cdn.xitu.io/2018/8/6/1650cff2cfd00353?w=282&h=500&f=gif&s=3963525)

- 共享元素跳转
  - ![共享元素跳转.gif](https://user-gold-cdn.xitu.io/2018/8/6/1650cff2d58d7b01?w=282&h=500&f=gif&s=4602043)

# How to use
### 图片选择器(集成了拍照和裁剪)
```
PicturePickerManager.with(this)
    .setThreshold(etAlbumThreshold.text.toString().toInt())// 一共选中的数量
    .setSpanCount(etSpanCount.text.toString().toInt())// 每行展示的数目
    .isShowScrollBehavior(checkboxAnimation.isChecked)// Behavior 动画
    .setToolbarBackgroundColorRes(R.color.colorPrimary) // Toolbar 背景设置
    .setIndicatorSolidColorRes(R.color.colorPrimary)// 选中指示器的颜色
    .setIndicatorBorderColorRes(R.color.colorPrimary, android.R.color.white)// 指示器边界的颜色
    .setPickerItemBackgroundColorRes(android.R.color.white)// 条目背景色
    // 开启相机支持
    .setCameraSupport(checkboxCamera.isChecked)
    .setFileProviderAuthority("$packageName.FileProvider")// 指定 FileProvider 的 authority, 用于 7.0 获取文件 URI
    .setCameraQuality(80)
    .setCameraDirectory(APP_DIRECTORY)// 相机文件存储路径
    // 开启图片裁剪支持(开启裁剪, 图片最多选择一张)
    .setCropSupport(checkboxCrop.isChecked)
    .setCropDirectory(APP_DIRECTORY)// 裁剪文件存储路径
    .setCropSize(1000, 1000)
    .setCropQuality(80)
    // 图片加载框架注入
    .setPictureLoader { context, uri, imageView -> Glide.with(context).load(uri).into(imageView) }
    // 获取图片选择的回调
    .start { it.forEach { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() } }
```
### 相机(集成了裁剪)
```
PictureTakeManager.with(this)
    .setFileProviderAuthority("$packageName.FileProvider")// 指定 FileProvider 的 authority, 用于 7.0 获取文件 URI
    .setCameraDirectory(APP_DIRECTORY)
    .setCameraQuality(80)
    // 拍照集成裁剪
    .setCropSupport(true)
    .setCropDirectory(APP_DIRECTORY)
    .setCropSize(1000, 1000)
    .setCropQuality(80)
    // 获取拍照后的图片回调
    .take { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
```  
### 裁剪
```
PictureCropManager.with(this)
    .setFileProviderAuthority("$packageName.FileProvider")
    .setCropSize(1000, 1000)// 裁剪框的尺寸
    .setCropQuality(80)// 裁剪后压缩的质量
    .setOriginFile(ORIGIN_IMAGE_PATH)// 裁剪图片的路径
    .setCropDirectory(APP_DIRECTORY)// 裁剪后图片存储目录
    .crop {Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
```
### 图片查看器
```
PictureWatcherManager.with(this)
    .setIndicatorBorderColorRes(R.color.colorPrimary, android.R.color.white)// 指示器边界的颜色
    .setIndicatorSolidColorRes(R.color.colorPrimary)// 选中指示器的颜色
    .setIndicatorTextColorRes(android.R.color.white)// 指示器文本颜色
    .setPictureUris(uris, 0)// 需要展示图片集合
    .setSharedElement(imageView)// 共享元素
    .setPictureLoader{ context, uri, imageView -> Glide.with(context).load(uri).into(imageView) }
    .start()
```
