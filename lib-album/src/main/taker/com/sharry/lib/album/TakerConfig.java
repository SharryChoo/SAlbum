package com.sharry.lib.album;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.sharry.lib.camera.IPreviewer;
import com.sharry.lib.media.recorder.Options;

/**
 * 相机拍照的相关参数
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/6/21 09:07
 */
public class TakerConfig implements Parcelable {

    protected TakerConfig(Parcel in) {
        authority = in.readString();
        pictureQuality = in.readInt();
        relativePath = in.readString();
        previewAspect = in.readInt();
        isFullScreen = in.readByte() != 0;
        isSupportVideoRecord = in.readByte() != 0;
        maximumDuration = in.readLong();
        minimumDuration = in.readLong();
        recordProgressColor = in.readInt();
        recordResolution = in.readInt();
        rendererClsName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authority);
        dest.writeInt(pictureQuality);
        dest.writeString(relativePath);
        dest.writeInt(previewAspect);
        dest.writeByte((byte) (isFullScreen ? 1 : 0));
        dest.writeByte((byte) (isSupportVideoRecord ? 1 : 0));
        dest.writeLong(maximumDuration);
        dest.writeLong(minimumDuration);
        dest.writeInt(recordProgressColor);
        dest.writeInt(recordResolution);
        dest.writeString(rendererClsName);
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
     * fileProvider 的 authority 属性, 用于 7.0 之后, 查找文件的 URI
     */
    private String authority;

    /**
     * 拍照后压缩的质量
     */
    private int pictureQuality = 80;

    /**
     * 文件输出路径
     */
    private String relativePath;

    public static final int ASPECT_1_1 = 758;
    public static final int ASPECT_4_3 = 917;
    public static final int ASPECT_16_9 = 995;


    @IntDef(value = {
            ASPECT_1_1,
            ASPECT_4_3,
            ASPECT_16_9
    })
    @interface Aspect {

    }

    /**
     * 初始时相机的预览比例
     */
    private int previewAspect = ASPECT_4_3;

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
    private long maximumDuration = 15 * 1000;

    /**
     * 视频录制最短时长
     * <p>
     * Unit is ms
     */
    private long minimumDuration = 1000;

    /**
     * 录制时进度条的颜色
     */
    private int recordProgressColor = Color.parseColor("#ff00b0ff");

    /**
     * 视频录制的最大分辨率
     * <p>
     * 默认为 720p
     */
    private int recordResolution = Options.Video.RESOLUTION_720P;

    /**
     * 用户自定义 Renderer 的类名
     */
    private String rendererClsName;

    private TakerConfig() {
    }

    public Builder rebuild() {
        return new Builder(this);
    }

    public int getPictureQuality() {
        return pictureQuality;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public int getPreviewAspect() {
        return previewAspect;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public boolean isSupportVideoRecord() {
        return isSupportVideoRecord;
    }

    public long getMaximumDuration() {
        return maximumDuration;
    }

    public long getMinimumDuration() {
        return minimumDuration;
    }

    public int getRecordProgressColor() {
        return recordProgressColor;
    }

    public String getRendererClassName() {
        return rendererClsName;
    }

    public int getRecordResolution() {
        return recordResolution;
    }

    public String getAuthority() {
        return authority;
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
        public Builder setRelativePath(@NonNull String dirPath) {
            Preconditions.checkNotEmpty(dirPath);
            this.mConfig.relativePath = dirPath;
            return this;
        }

        /**
         * 设置文件输出的目录, 拍摄后的图片会生成在目录下
         */
        public Builder setAuthority(@NonNull String authority) {
            Preconditions.checkNotEmpty(authority);
            this.mConfig.authority = authority;
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
        public Builder setPreviewAspect(@Aspect int aspect) {
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
            mConfig.maximumDuration = maxRecordDuration;
            return this;
        }

        /**
         * 设置录制最短时长
         *
         * @param minimumDuration unit ms
         */
        public Builder setMinRecordDuration(long minimumDuration) {
            mConfig.minimumDuration = minimumDuration;
            return this;
        }

        /**
         * 设置录制进度条的颜色
         */
        public Builder setRecordProgressColor(@ColorInt int colorRecordProgress) {
            mConfig.recordProgressColor = colorRecordProgress;
            return this;
        }

        /**
         * 设置用户的自定义 Renderer
         */
        public Builder setRenderer(@NonNull Class<? extends IPreviewer.Renderer> rendererClass) {
            try {
                rendererClass.getDeclaredConstructor(Context.class);
            } catch (NoSuchMethodException e) {
                throw new UnsupportedOperationException("Please ensure " + rendererClass.getSimpleName()
                        + " have a constructor like: " + rendererClass.getSimpleName() + "(Context context)");
            }
            mConfig.rendererClsName = rendererClass.getName();
            return this;
        }

        /**
         * 设置视频录制时的分辨率
         */
        public Builder setRecordResolution(@Options.Video.Resolution int recordResolution) {
            mConfig.recordResolution = recordResolution;
            return this;
        }

        public TakerConfig build() {
            return mConfig;
        }

    }

}
