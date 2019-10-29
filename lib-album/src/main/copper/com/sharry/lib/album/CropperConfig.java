package com.sharry.lib.album;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 图片裁剪的相关参数
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.1
 * @since 2018/11/29 16:57
 */
public class CropperConfig implements Parcelable {

    protected CropperConfig(Parcel in) {
        originUri = in.readParcelable(Uri.class.getClassLoader());
        relativePath = in.readString();
        isCropCircle = in.readByte() != 0;
        authority = in.readString();
        aspectX = in.readInt();
        aspectY = in.readInt();
        outputX = in.readInt();
        outputY = in.readInt();
        destQuality = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(originUri, flags);
        dest.writeString(relativePath);
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

    public static final Creator<CropperConfig> CREATOR = new Creator<CropperConfig>() {
        @Override
        public CropperConfig createFromParcel(Parcel in) {
            return new CropperConfig(in);
        }

        @Override
        public CropperConfig[] newArray(int size) {
            return new CropperConfig[size];
        }
    };

    public static Builder Builder() {
        return new Builder();
    }

    private Uri originUri;              // 需要裁剪的图片路径
    private String relativePath;   // 可用的输出目录
    private boolean isCropCircle;       // 是否为圆形裁剪
    private String authority;           // fileProvider 的 authority 属性, 用于 7.0 之后, 查找文件的 URI
    private int aspectX = 1;            // 方形 X 的比率
    private int aspectY = 1;            // 方形 X 的比率
    private int outputX = 500;          // 图像输出时的宽
    private int outputY = 500;          // 图像输出的高
    private int destQuality = 80;       // 裁剪后图片输出的质量

    private CropperConfig() {
    }

    public Uri getOriginUri() {
        return originUri;
    }

    public String getRelativePath() {
        return relativePath;
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

    public Builder rebuild() {
        return new Builder(this);
    }

    /**
     * 构建 Config 对象
     */
    public static class Builder {

        private CropperConfig mConfig;

        private Builder() {
            mConfig = new CropperConfig();
        }

        private Builder(@NonNull CropperConfig config) {
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
        public Builder setAuthority(@NonNull String authorities) {
            Preconditions.checkNotEmpty(authorities);
            mConfig.authority = authorities;
            return this;
        }

        /**
         * 设置需要裁剪的文件地址
         */
        public Builder setOriginFile(@NonNull Uri originUri) {
            this.mConfig.originUri = originUri;
            return this;
        }

        /**
         * 设置文件输出相对路径, 拍摄后的图片会生成在目录下
         * <p>
         * 绝对路径: "/storage/emulated/0/SAlbum"
         * 相对路径: "SAlbum"
         * <p>
         * 注:
         * Android 10 无法在外部存储卡随意创建文件, 因此会在对应的媒体目录下追加相对路径
         * 如: "/storage/emulated/0/" + {@link android.os.Environment#DIRECTORY_PICTURES} + "SAlbum"
         *
         * @param relativePath 若是传 null, 则会在 {@link Context#getExternalFilesDir(String)} 中创建,
         *                     在该目录中创建媒体文件无法在 MediaStore 中显示
         */
        public Builder setRelativePath(@Nullable String relativePath) {
            this.mConfig.relativePath = relativePath;
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
        public CropperConfig build() {
            if (TextUtils.isEmpty(mConfig.authority)) {
                throw new UnsupportedOperationException("Please invoke setAuthority correct");
            }
            return mConfig;
        }
    }
}
