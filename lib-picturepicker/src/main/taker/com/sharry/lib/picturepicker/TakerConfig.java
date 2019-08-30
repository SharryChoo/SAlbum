package com.sharry.lib.picturepicker;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 相机拍照的相关参数
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/6/21 09:07
 */
public class TakerConfig implements Parcelable {

    /**
     * Get instance of TakerConfig.Builder.
     */
    @NonNull
    public static Builder Builder() {
        return new Builder();
    }

    public static final Creator<TakerConfig> CREATOR = new Creator<TakerConfig>() {
        @Override
        public TakerConfig createFromParcel(Parcel in) {
            return new TakerConfig(in);
        }

        @Override
        public TakerConfig[] newArray(int size) {
            return new TakerConfig[size];
        }
    };

    private String authority;                 // fileProvider 的 authority 属性, 用于 7.0 之后, 查找文件的 URI
    private int cameraDestQuality = 80;       // 拍照后压缩的质量
    private String cameraDirectoryPath;       // 存储文件的目录路径
    private CropperConfig cropperConfig;            // 图片裁剪的 Config

    private TakerConfig() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authority);
        dest.writeInt(cameraDestQuality);
        dest.writeString(cameraDirectoryPath);
        dest.writeParcelable(cropperConfig, flags);
    }

    /**
     * 是否支持裁剪
     */
    public boolean isCropSupport() {
        return cropperConfig != null;
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

    public CropperConfig getCropperConfig() {
        return cropperConfig;
    }

    protected TakerConfig(Parcel in) {
        authority = in.readString();
        cameraDestQuality = in.readInt();
        cameraDirectoryPath = in.readString();
        cropperConfig = in.readParcelable(CropperConfig.class.getClassLoader());
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

        private TakerConfig mConfig;

        private Builder() {
            mConfig = new TakerConfig();
        }

        private Builder(@NonNull TakerConfig config) {
            this.mConfig = config;
        }

        /**
         * 设置目的文件
         */
        public Builder setCameraDirectory(@NonNull String filePath) {
            Preconditions.checkNotEmpty(filePath);
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
            Preconditions.checkNotEmpty(authorities);
            mConfig.authority = authorities;
            return this;
        }

        /**
         * 设置裁剪的配置
         */
        public Builder setCropConfig(@Nullable CropperConfig cropperConfig) {
            mConfig.cropperConfig = cropperConfig;
            return this;
        }

        public TakerConfig build() {
            return mConfig;
        }
    }

}
