package com.sharry.picturepicker.crop;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * 图片裁剪的相关参数
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.1
 * @since 2018/11/29 16:57
 */
public class CropConfig implements Parcelable {

    public static Builder Builder() {
        return new Builder();
    }

    public static final Creator<CropConfig> CREATOR = new Creator<CropConfig>() {
        @Override
        public CropConfig createFromParcel(Parcel in) {
            return new CropConfig(in);
        }

        @Override
        public CropConfig[] newArray(int size) {
            return new CropConfig[size];
        }
    };

    private String originFilePath;      // 需要裁剪的图片路径
    private String cropDirectoryPath;   // 可用的输出目录
    private boolean isCropCircle;       // 是否为圆形裁剪
    private String authority;           // fileProvider 的 authority 属性, 用于 7.0 之后, 查找文件的 URI
    private int aspectX = 1;            // 方形 X 的比率
    private int aspectY = 1;            // 方形 X 的比率
    private int outputX = 500;          // 图像输出时的宽
    private int outputY = 500;          // 图像输出的高
    private int destQuality = 80;       // 裁剪后图片输出的质量

    private CropConfig() {
    }

    public String getOriginFilePath() {
        return originFilePath;
    }

    public String getCropDirectoryPath() {
        return cropDirectoryPath;
    }

    public boolean isCropCircle() {
        return isCropCircle;
    }

    public String getAuthority() {
        return authority;
    }

    public int getAspectX() {
        return aspectX;
    }

    public int getAspectY() {
        return aspectY;
    }

    public int getOutputX() {
        return outputX;
    }

    public int getOutputY() {
        return outputY;
    }

    public int getDestQuality() {
        return destQuality;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originFilePath);
        dest.writeString(cropDirectoryPath);
        dest.writeByte((byte) (isCropCircle ? 1 : 0));
        dest.writeString(authority);
        dest.writeInt(aspectX);
        dest.writeInt(aspectY);
        dest.writeInt(outputX);
        dest.writeInt(outputY);
        dest.writeInt(destQuality);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Builder rebuild() {
        return new Builder(this);
    }

    protected CropConfig(Parcel in) {
        originFilePath = in.readString();
        cropDirectoryPath = in.readString();
        isCropCircle = in.readByte() != 0;
        authority = in.readString();
        aspectX = in.readInt();
        aspectY = in.readInt();
        outputX = in.readInt();
        outputY = in.readInt();
        destQuality = in.readInt();
    }

    /**
     * 构建 Config 对象
     */
    public static class Builder {

        private CropConfig mConfig;

        private Builder() {
            mConfig = new CropConfig();
        }

        private Builder(@NonNull CropConfig config) {
            this.mConfig = config;
        }

        /**
         * 设置是否为圆形裁剪区域
         */
        public Builder setCropCircle(boolean isCropCircle) {
            this.mConfig.isCropCircle = isCropCircle;
            return this;
        }

        /**
         * 设置裁剪的尺寸
         */
        public Builder setCropSize(int width, int height) {
            this.mConfig.outputX = width;
            this.mConfig.outputY = height;
            return this;
        }

        /**
         * 设置裁剪的比例
         */
        public Builder setAspectSize(int x, int y) {
            this.mConfig.aspectX = x;
            this.mConfig.aspectY = y;
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
         * 设置需要裁剪的文件地址
         */
        public Builder setOriginFile(@NonNull String filePath) {
            this.mConfig.originFilePath = filePath;
            return this;
        }

        /**
         * 设置需要裁剪的文件地址
         */
        public Builder setCropDirectory(@NonNull String filePath) {
            this.mConfig.cropDirectoryPath = filePath;
            return this;
        }

        /**
         * 设置裁剪后压缩的质量
         */
        public Builder setCropQuality(int quality) {
            mConfig.destQuality = quality;
            return this;
        }

        @NonNull
        public CropConfig build() {
            return mConfig;
        }
    }
}
