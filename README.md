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
[![](https://jitpack.io/v/SharryChoo/SPicturePicker.svg)](https://jitpack.io/#SharryChoo/SPicturePicker)

### Step 1
Add it in your **module build.gradle** at the end of repositories
```
dependencies {
    ...
    implementation 'com.github.SharryChoo:PicturePicker:+'
    // Android dependencies
    def constraintlayoutVersion = "1.1.3"
    implementation "androidx.constraintlayout:constraintlayout:$constraintlayoutVersion"
    def supportLibraryVersion = '1.1.0'
    implementation "androidx.appcompat:appcompat:$supportLibraryVersion"
    def recycleViewVersion = '1.0.0'
    implementation "androidx.recyclerview:recyclerview:$recycleViewVersion"
    def materialVersion = '1.0.0'
    implementation "com.google.android.material:material:$materialVersion"
    def cameraxVersion = "1.0.0-alpha04"
    implementation "androidx.camera:camera-core:$cameraxVersion"
    implementation "androidx.camera:camera-camera2:$cameraxVersion"
    
    // My dependencies.
    def stoolbarVersion = "1.0.5-x"
    api "com.github.SharryChoo:SToolbar:$stoolbarVersion"
    def smediaVersion = "0.0.9-alpha"
    api "com.github.SharryChoo.SMedia:lib-media-recorder:$smediaVersion"
    api "com.github.SharryChoo.SMedia:lib-opengles:$smediaVersion"
    api "com.github.SharryChoo.SMedia:lib-scamera:$smediaVersion"     
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

// CameraX 处于 alpha 版本, 需要解决 androidx 的冲突问题
subprojects {
    configurations.all {
        resolutionStrategy {
            force "androidx.core:core:${supportLibraryVersion}"
        }
    }
}
```

### Step 3
Add it in your **module AndroidManifest.xml**
```
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="......">
    
    <!--解决 minSdkVersion 在 21 以下, CameraX 编译无法通过的问题-->
    <uses-sdk tools:overrideLibrary="androidx.camera.camera2, androidx.camera.core" />
  
</manifest>
```

## 功能使用
