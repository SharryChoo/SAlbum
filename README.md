## 功能介绍
- 图片的选取
  - 支持 JPEG/PNG/WEBP/GIF 的选取
  - 图片加载引擎由用户自定义实现
- 图片的查看
  - 共享元素跳转
- 相机的拍摄
  - 提供了自定义 Renderer 的接口
  - 由用户自定义实现滤镜效果
- 视频的录制
  - 支持 1080p, 720p, 480p 的录制分辨率
  - 默认为 720p
- 视频的播放
- Material Design 动画

## 功能集成
[![](https://jitpack.io/v/SharryChoo/PicturePicker.svg)](https://jitpack.io/#SharryChoo/PicturePicker)

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
    implementation 'com.github.SharryChoo:PicturePicker:+'
    implementation "androidx.appcompat:appcompat:1.0.0"
    implementation "androidx.recyclerview:recyclerview:1.0.0"
    implementation "com.google.android.material:material:1.0.0"
}
```

## 功能使用
