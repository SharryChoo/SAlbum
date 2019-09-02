# CMake 最小编译版本
CMAKE_MINIMUM_REQUIRED(VERSION 3.4.1)

# 指定需要使用的头文件
INCLUDE_DIRECTORIES(${PROJECT_SOURCE_DIR}/src/main/jniLibs/include)
# 指定外部链接库目录
LINK_DIRECTORIES(${PROJECT_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a)

# 添加要打包的资源
FILE(GLOB SRC_LISTS "${PROJECT_SOURCE_DIR}/src/main/cpp/*.cpp" "${PROJECT_SOURCE_DIR}/src/main/cpp/*.c")
FILE(GLOB SOUND_TOUCH_LISTS "${PROJECT_SOURCE_DIR}/src/main/cpp/soundtouch/*.cpp"
        "${PROJECT_SOURCE_DIR}/src/main/cpp/soundtouch/*.c")
add_library(
        smedia-player
        SHARED
        ${SRC_LISTS}
        ${SOUND_TOUCH_LISTS}
)

# 将打包的 so 链接到项目中
target_link_libraries(
        # 目标库
        smedia-player
        # other libs
        avcodec-57       # 编码库
        avdevice-57      # 设备信息库
        avfilter-6       # 滤镜特效处理库
        avformat-57      # 封装格式处理库
        avutil-55        # 工具库
        postproc-54      # 后期处理库
        swresample-2     # 音频采样数据格式转换库
        swscale-4        # 视频像素数据格式转换
        # Android libs
        log
        android
        OpenSLES
)