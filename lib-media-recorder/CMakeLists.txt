# CMake 最小编译版本
CMAKE_MINIMUM_REQUIRED(VERSION 3.4.1)

# 添加要打包的资源
FILE(GLOB SRC_LISTS "${PROJECT_SOURCE_DIR}/src/main/cpp/*.cpp" "${PROJECT_SOURCE_DIR}/src/main/cpp/*.c")

add_library(
        smedia-recorder
        SHARED
        ${SRC_LISTS}
)

# 将打包的 so 链接到项目中
target_link_libraries(
        # 目标库
        smedia-recorder
        # Android libs
        log
        android
        OpenSLES
)