## 功能介绍
- 图片的选取
  - 支持 JPEG/PNG/WEBP/GIF 的选取
  - 图片加载引擎由用户自定义实现
- 图片的查看
  - 共享元素跳转动画
- 图像的裁剪
- **相机的拍摄**
  - **5.0 以下使用 Camera1, 5.0 以上使用 CameraX**
  - **提供 1:1、4:3、16:9 的比例选择**
     - 支持 CenterCrop 全屏预览
  - **通过自定义 Renderer, 可拓展水印滤镜等效果**
- **视频的录制**
  - 视频
    - **使用共享 EGLContext 的方式配合 MediaCodec 实现 H.264 的硬编, 支持滤镜水印效果**
    - 支持 1080p, 720p, 480p 的录制分辨率
  - 音频
    - **PCM 数据获取使用 OpenSL ES, 支持 v7a**
    - 使用 MediaCodec 硬编为 AAC
  - 使用 MediaMuxer 合并为 mp4 文件
- 视频的播放
  - 考虑到依赖体积, 使用系统提供的 VideoView 实现

## 功能集成
[![](https://jitpack.io/v/SharryChoo/SPicturePicker.svg)](https://jitpack.io/#SharryChoo/SPicturePicker)

### Step 1
Add it in your **module build.gradle** at the end of repositories
```
dependencies {
    ...
    // SPicturePicker dependencies
    implementation 'com.github.SharryChoo:SPicturePicker:+'
    
    // Needed Android dependencies
    def constraintlayoutVersion = "1.1.3"
    implementation "androidx.constraintlayout:constraintlayout:$constraintlayoutVersion"
    def supportLibraryVersion = '1.1.0'
    implementation "androidx.appcompat:appcompat:$supportLibraryVersion"
    def recycleViewVersion = '1.0.0'
    implementation "androidx.recyclerview:recyclerview:$recycleViewVersion"
    def materialVersion = '1.0.0'
    implementation "com.google.android.material:material:$materialVersion"
    def cameraxVersion = "1.0.0-alpha03"// alpha04 在尺寸选取上存在非常大的问题, 这里可以先使用 03
    implementation "androidx.camera:camera-core:$cameraxVersion"
    implementation "androidx.camera:camera-camera2:$cameraxVersion"
    
    // Needed core dependencies.
    def stoolbarVersion = "1.0.5-x"
    api "com.github.SharryChoo:SToolbar:$stoolbarVersion"
    def smediaVersion = "1.0.0-alpha01"
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
    
    <!--解决 minSdkVersion 在 21 以下, CameraX 编译报错的问题-->
    <uses-sdk tools:overrideLibrary="androidx.camera.camera2, androidx.camera.core" />
  
</manifest>
```

## 效果展示
### 资源选取

### 图像拍摄

### 视频录制

### 视频播放

## 功能使用
