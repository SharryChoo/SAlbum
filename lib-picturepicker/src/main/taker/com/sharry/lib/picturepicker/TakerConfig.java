package com.sharry.lib.picturepicker;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sharry.lib.camera.AspectRatio;

/**
 * 相机拍照的相关参数
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/6/21 09:07
 */
public class TakerConfig implements Parcelable {

    protected TakerConfig(Parcel in) {
        pictureQuality = in.readInt();
        authority = in.readString();
        directoryPath = in.readString();
        cropperConfig = in.readParcelable(CropperConfig.class.getClassLoader());
        previewAspect = in.readParcelable(AspectRatio.class.getClassLoader());
        isFullScreen = in.readByte() != 0;
        isSupportVideoRecord = in.readByte() != 0;
        maxRecordDuration = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pictureQuality);
        dest.writeString(authority);
        dest.writeString(directoryPath);
        dest.writeParcelable(cropperConfig, flags);
        dest.writeParcelable(previewAspect, flags);
        dest.writeByte((byte) (isFullScreen ? 1 : 0));
        dest.writeByte((byte) (isSupportVideoRecord ? 1 : 0));
        dest.writeLong(maxRecordDuration);
    }

    @Override
    public int describeContents() {
        return 0;
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

    /**
     * Get instance of TakerConfig.Builder.
     */
    @NonNull
    public static Builder Builder() {
        return new Builder();
    }

    /**
     * 拍照后压缩的质量
     */
    private int pictureQuality = 80;

    /**
     * fileProvider 的 authority 属性, 用于 7.0 之后, 查找文件的 URI
     */
    private String authority;

    /**
     * 文件输出路径
     */
    private String directoryPath;

    /**
     * 裁剪配置
     */
    private CropperConfig cropperConfig;

    /**
     * 初始时相机的预览比例
     */
    private AspectRatio previewAspect;

    /**
     * 相机预览时是否缩放至全屏
     */
    private boolean isFullScreen;

    /**
     * 是否支持视频录制
     * <p>
     * 若支持视频录制, 则裁剪无效
     */
    private boolean isSupportVideoRecord;

    /**
     * 视频录制最大时长
     * <p>
     * Unit is ms
     */
    private long maxRecordDuration = 15 * 1000;

    private TakerConfig() {
    }

    public Builder rebuild() {
        return new Builder(this);
    }

    public boolean isCropSupport() {
        return cropperConfig != null;
    }

    public int getPictureQuality() {
        return pictureQuality;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public CropperConfig getCropperConfig() {
        return cropperConfig;
    }

    public AspectRatio getPreviewAspect() {
        return previewAspect;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public boolean isSupportVideoRecord() {
        return isSupportVideoRecord;
    }

    public String getAuthority() {
        return authority;
    }

    public long getMaxRecordDuration() {
        return maxRecordDuration;
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
         * 设置文件输出的目录, 拍摄后的图片会生成在目录下
         */
        public Builder setDirectoryPath(@NonNull String dirPath) {
            Preconditions.checkNotEmpty(dirPath);
            this.mConfig.directoryPath = dirPath;
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
         * 设置拍照后的压缩质量
         */
        public Builder setPictureQuality(int quality) {
            mConfig.pictureQuality = quality;
            return this;
        }

        /**
         * 设置裁剪的配置
         */
        public Builder setCropConfig(@Nullable CropperConfig cropperConfig) {
            mConfig.cropperConfig = cropperConfig;
            return this;
        }

        /**
         * 设置裁剪的配置
         */
        public Builder setPreviewAspect(@Nullable AspectRatio aspect) {
            mConfig.previewAspect = aspect;
            return this;
        }

        /**
         * 设置裁剪的配置
         */
        public Builder setFullScreen(boolean isFullScreen) {
            mConfig.isFullScreen = isFullScreen;
            return this;
        }

        /**
         * 是否支持录制是否
         *
         * @param isSupportVideoRecord true is support, false is just take picture.
         */
        public Builder setVideoRecord(boolean isSupportVideoRecord) {
            mConfig.isSupportVideoRecord = isSupportVideoRecord;
            return this;
        }

        /**
         * 设置录制最大时长
         *
         * @param maxRecordDuration unit ms
         */
        public Builder setMaxRecordDuration(long maxRecordDuration) {
            mConfig.maxRecordDuration = maxRecordDuration;
            return this;
        }

        public TakerConfig build() {
            return mConfig;
        }
    }

}
