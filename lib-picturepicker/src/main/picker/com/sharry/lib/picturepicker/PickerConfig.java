package com.sharry.lib.picturepicker;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * 图片选择器的配置属性类
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.1
 * @since 2018/11/29 17:07
 */
public class PickerConfig implements Parcelable {


    static final int INVALIDATE_VALUE = -1;
    static final int COLOR_DEFAULT = Color.parseColor("#ff64b6f6");

    protected PickerConfig(Parcel in) {
        userPickedSet = in.createTypedArrayList(MediaMeta.CREATOR);
        threshold = in.readInt();
        spanCount = in.readInt();
        toolbarBkgColor = in.readInt();
        toolbarBkgDrawableResId = in.readInt();
        pickerBackgroundColor = in.readInt();
        pickerItemBackgroundColor = in.readInt();
        indicatorTextColor = in.readInt();
        indicatorSolidColor = in.readInt();
        indicatorBorderCheckedColor = in.readInt();
        indicatorBorderUncheckedColor = in.readInt();
        isToolbarBehavior = in.readByte() != 0;
        isFabBehavior = in.readByte() != 0;
        isPickVideo = in.readByte() != 0;
        isPickGif = in.readByte() != 0;
        takerConfig = in.readParcelable(TakerConfig.class.getClassLoader());
        cropperConfig = in.readParcelable(CropperConfig.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(userPickedSet);
        dest.writeInt(threshold);
        dest.writeInt(spanCount);
        dest.writeInt(toolbarBkgColor);
        dest.writeInt(toolbarBkgDrawableResId);
        dest.writeInt(pickerBackgroundColor);
        dest.writeInt(pickerItemBackgroundColor);
        dest.writeInt(indicatorTextColor);
        dest.writeInt(indicatorSolidColor);
        dest.writeInt(indicatorBorderCheckedColor);
        dest.writeInt(indicatorBorderUncheckedColor);
        dest.writeByte((byte) (isToolbarBehavior ? 1 : 0));
        dest.writeByte((byte) (isFabBehavior ? 1 : 0));
        dest.writeByte((byte) (isPickVideo ? 1 : 0));
        dest.writeByte((byte) (isPickGif ? 1 : 0));
        dest.writeParcelable(takerConfig, flags);
        dest.writeParcelable(cropperConfig, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PickerConfig> CREATOR = new Creator<PickerConfig>() {
        @Override
        public PickerConfig createFromParcel(Parcel in) {
            return new PickerConfig(in);
        }

        @Override
        public PickerConfig[] newArray(int size) {
            return new PickerConfig[size];
        }
    };

    public static Builder Builder() {
        return new Builder();
    }

    /**
     * 用户已经选中的集合
     */
    private ArrayList<MediaMeta> userPickedSet = new ArrayList<>();

    /**
     * 最大选取阈值
     */
    private int threshold = 9;

    /**
     * 每行展示数量
     */
    private int spanCount = 3;

    /**
     * Toolbar 背景
     */
    private int toolbarBkgColor = COLOR_DEFAULT;
    private int toolbarBkgDrawableResId = INVALIDATE_VALUE;

    /**
     * 整体背景色
     */
    private int pickerBackgroundColor = INVALIDATE_VALUE;
    private int pickerItemBackgroundColor = Color.WHITE;

    /**
     * 指示器背景色
     */
    private int indicatorTextColor = Color.WHITE;
    private int indicatorSolidColor = COLOR_DEFAULT;
    private int indicatorBorderCheckedColor = indicatorSolidColor;
    private int indicatorBorderUncheckedColor = Color.WHITE;

    /**
     * 控制 Flag
     */
    private boolean isToolbarBehavior = false;
    private boolean isFabBehavior = false;
    private boolean isPickVideo = false;
    private boolean isPickGif = false;

    /**
     * 其他功能的 Config
     */
    private TakerConfig takerConfig;
    private CropperConfig cropperConfig;

    private PickerConfig() {
    }

    @NonNull
    public ArrayList<MediaMeta> getUserPickedSet() {
        return userPickedSet;
    }

    public int getThreshold() {
        return threshold;
    }

    public int getSpanCount() {
        return spanCount;
    }

    public int getToolbarBkgColor() {
        return toolbarBkgColor;
    }

    public int getToolbarBkgDrawableResId() {
        return toolbarBkgDrawableResId;
    }

    public int getPickerBackgroundColor() {
        return pickerBackgroundColor;
    }

    public int getPickerItemBackgroundColor() {
        return pickerItemBackgroundColor;
    }

    public int getIndicatorTextColor() {
        return indicatorTextColor;
    }

    public int getIndicatorSolidColor() {
        return indicatorSolidColor;
    }

    public int getIndicatorBorderCheckedColor() {
        return indicatorBorderCheckedColor;
    }

    public int getIndicatorBorderUncheckedColor() {
        return indicatorBorderUncheckedColor;
    }

    public boolean isToolbarBehavior() {
        return isToolbarBehavior;
    }

    public boolean isFabBehavior() {
        return isFabBehavior;
    }

    public boolean isPickVideo() {
        return isPickVideo;
    }

    public boolean isPickGif() {
        return isPickGif;
    }

    @Nullable
    public TakerConfig getTakerConfig() {
        return takerConfig;
    }

    @Nullable
    public CropperConfig getCropperConfig() {
        return cropperConfig;
    }

    /**
     * 是否支持相机
     */
    public boolean isCameraSupport() {
        return takerConfig != null;
    }

    /**
     * 是否支持裁剪
     */
    public boolean isCropSupport() {
        return cropperConfig != null;
    }

    public Builder rebuild() {
        return new Builder(this);
    }

    public static class Builder {

        private PickerConfig mConfig;

        private Builder() {
            mConfig = new PickerConfig();
        }

        private Builder(@NonNull PickerConfig config) {
            Preconditions.checkNotNull(config);
            this.mConfig = config;
        }

        /**
         * 设置相册可选的最大数量
         *
         * @param threshold 阈值
         */
        public Builder setThreshold(int threshold) {
            mConfig.threshold = threshold;
            return this;
        }

        /**
         * 设置用户已经选中的图片, 相册会根据 Path 比较, 在相册中打钩
         *
         * @param pickedPictures 已选中的图片
         */
        public Builder setPickedPictures(@Nullable ArrayList<MediaMeta> pickedPictures) {
            if (null != pickedPictures) {
                mConfig.userPickedSet.addAll(pickedPictures);
            }
            return this;
        }

        public Builder setSpanCount(int count) {
            mConfig.spanCount = count;
            return this;
        }

        /**
         * 设置 Toolbar 的背景色
         */
        public Builder setToolbarBackgroundColor(@ColorInt int color) {
            mConfig.toolbarBkgColor = color;
            return this;
        }

        /**
         * 设置 Toolbar 的背景图片
         *
         * @param drawableRes drawable 资源 ID
         */
        public Builder setToolbarBackgroundDrawableRes(@DrawableRes int drawableRes) {
            mConfig.toolbarBkgDrawableResId = drawableRes;
            return this;
        }

        /**
         * 设置图片选择器的背景色
         */
        public Builder setPickerBackgroundColor(@ColorInt int color) {
            mConfig.pickerBackgroundColor = color;
            return this;
        }

        /**
         * 设置图片选择器的背景色
         */
        public Builder setPickerItemBackgroundColor(@ColorInt int color) {
            mConfig.pickerItemBackgroundColor = color;
            return this;
        }

        /**
         * 设置选择索引的边框颜色
         *
         * @param textColor 边框的颜色
         */
        public Builder setIndicatorTextColor(@ColorInt int textColor) {
            mConfig.indicatorTextColor = textColor;
            return this;
        }

        /**
         * 设置选择索引的边框颜色
         *
         * @param solidColor 边框的颜色
         */
        public Builder setIndicatorSolidColor(@ColorInt int solidColor) {
            mConfig.indicatorSolidColor = solidColor;
            return this;
        }

        /**
         * 设置选择索引的边框颜色
         *
         * @param checkedColor   选中的边框颜色的 Res Id
         * @param uncheckedColor 未选中的边框颜色的Res Id
         */
        public Builder setIndicatorBorderColor(@ColorInt int checkedColor, @ColorInt int uncheckedColor) {
            mConfig.indicatorBorderCheckedColor = checkedColor;
            mConfig.indicatorBorderUncheckedColor = uncheckedColor;
            return this;
        }

        /**
         * 是否设置 Toolbar Behavior 动画
         */
        public Builder isToolbarScrollable(boolean isToolbarScrollable) {
            mConfig.isToolbarBehavior = isToolbarScrollable;
            return this;
        }

        /**
         * 是否设置 Fab Behavior 滚动动画
         */
        public Builder isFabScrollable(boolean isFabScrollable) {
            mConfig.isFabBehavior = isFabScrollable;
            return this;
        }

        /**
         * 是否支持选取视频
         *
         * @param isPickVideo if true is support.
         */
        public Builder isPickVideo(boolean isPickVideo) {
            mConfig.isPickVideo = isPickVideo;
            return this;
        }

        /**
         * 是否支持选取 GIF 图
         *
         * @param isPickGif if true is support.
         */
        public Builder isPickGif(boolean isPickGif) {
            mConfig.isPickGif = isPickGif;
            return this;
        }

        /**
         * 裁剪项的配置
         *
         * @param cropperConfig if null is deny crop, if not null is granted.
         */
        public Builder setCropConfig(@Nullable CropperConfig cropperConfig) {
            mConfig.cropperConfig = cropperConfig;
            return this;
        }

        /**
         * 拍摄项的配置
         *
         * @param takerConfig if null is deny taker, if not null is granted.
         */
        public Builder setCameraConfig(@Nullable TakerConfig takerConfig) {
            mConfig.takerConfig = takerConfig;
            return this;
        }

        public PickerConfig build() {
            return mConfig;
        }

    }

}
