package com.sharry.picturepicker.camera;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sharry.picturepicker.crop.CropConfig;

/**
 * Created by Sharry on 2018/6/21.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 相机拍照的相关参数
 */
public class CameraConfig implements Parcelable {

    /**
     * Get instance of CameraConfig.Builder.
     */
    @NonNull
    public static Builder Builder() {
        return new Builder();
    }

    public static final Creator<CameraConfig> CREATOR = new Creator<CameraConfig>() {
        @Override
        public CameraConfig createFromParcel(Parcel in) {
            return new CameraConfig(in);
        }

        @Override
        public CameraConfig[] newArray(int size) {
            return new CameraConfig[size];
        }
    };

    private String authority;                 // fileProvider 的 authority 属性, 用于 7.0 之后, 查找文件的 URI
    private int cameraDestQuality = 80;       // 拍照后压缩的质量
    private String cameraDirectoryPath;       // 存储文件的目录路径
    private CropConfig cropConfig;            // 图片裁剪的 Config

    private CameraConfig() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authority);
        dest.writeInt(cameraDestQuality);
        dest.writeString(cameraDirectoryPath);
        dest.writeParcelable(cropConfig, flags);
    }

    /**
     * 是否支持裁剪
     */
    public boolean isCropSupport() {
        return cropConfig != null;
    }

    public String getAuthority() {
        return authority;
    }

    public int getCameraDestQuality() {
        return cameraDestQuality;
    }

    public String getCameraDirectoryPath() {
        return cameraDirectoryPath;
    }

    public CropConfig getCropConfig() {
        return cropConfig;
    }

    protected CameraConfig(Parcel in) {
        authority = in.readString();
        cameraDestQuality = in.readInt();
        cameraDirectoryPath = in.readString();
        cropConfig = in.readParcelable(CropConfig.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 重新编辑当前对象
     */
    public Builder rebuild() {
        return new Builder(this);
    }

    public static class Builder {

        private CameraConfig mConfig;

        private Builder() {
            mConfig = new CameraConfig();
        }

        private Builder(@NonNull CameraConfig config) {
            this.mConfig = config;
        }

        /**
         * 设置目的文件
         */
        public Builder setCameraDirectory(@NonNull String filePath) {
            this.mConfig.cameraDirectoryPath = filePath;
            return this;
        }

        /**
         * 设置拍照后的压缩质量
         */
        public Builder setCameraQuality(int quality) {
            mConfig.cameraDestQuality = quality;
            return this;
        }

        /**
         * 设置 FileProvider 的路径, 7.0 以后用于查找 URI
         */
        public Builder setFileProviderAuthority(@NonNull String authorities) {
            mConfig.authority = authorities;
            return this;
        }

        /**
         * 设置裁剪的配置
         */
        public Builder setCropConfig(@Nullable CropConfig cropConfig) {
            mConfig.cropConfig = cropConfig;
            return this;
        }

        public CameraConfig build() {
            return mConfig;
        }
    }

}
