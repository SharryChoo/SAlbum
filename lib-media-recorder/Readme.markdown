# Module 主要功能
使用 Android 提供的 API 进行音视频的采集, 播放等操作

# 音视频采集
## 音频采集
- 使用了 AudioRecord 获取原生的 PCM 音频流

## 视频采集
- 使用了 Camera 采集, 使用 SurfaceTexture 渲染图像, 输出 YCbCr_420_SP 的视频流

# 音视频播放
## 音频播放
- 使用了 AudioTrack 播放原生的 PCM 音频流