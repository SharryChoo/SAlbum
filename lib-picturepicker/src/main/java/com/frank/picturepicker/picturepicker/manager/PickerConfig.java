package com.frank.picturepicker.picturepicker.manager;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/19.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片选择器的配置属性类
 */
public class PickerConfig implements Parcelable {

    public static final int INVALIDATE_VALUE = -1;

    // 图片选择的相关配置
    public ArrayList<String> userPickedSet = new ArrayList<>();
    public int threshold = 9;
    public int spanCount = 3;

    // Toolbar 背景色
    public int toolbarBkgColor = Color.parseColor("#ff64b6f6");// 背景色
    public int toolbarBkgDrawableResId = INVALIDATE_VALUE;// 背景的Drawable

    // 整体背景色
    public int pickerBackgroundColor = INVALIDATE_VALUE;

    // Item 背景色
    public int pickerItemBackgroundColor = Color.WHITE;

    // 指示器背景色
    public int indicatorTextColor = Color.WHITE;
    public int indicatorSolidColor = Color.parseColor("#ff64b6f6");// 指示器选中的填充色
    public int indicatorBorderCheckedColor = indicatorSolidColor;// 指示器边框选中的颜色
    public int indicatorBorderUncheckedColor = Color.WHITE;// 指示器边框未被选中的颜色

    // 是否展示滚动动画
    public boolean isShowToolbarBehavior = false;
    public boolean isShowFabBehavior = false;

    // 是否展示拍照支持
    public String authority;// FileProvider 的 authority 属性
    public boolean isCameraSupport = false;// 是否展示拍照功能
    public int cameraIconDrawableResId = INVALIDATE_VALUE;// 相机的资源文件
    public String cameraDirectoryPath;// 拍照后的路径
    public int cameraQuality;// 拍照照片的质量

    // 是否裁剪支持
    public boolean isCropSupport = false;// 照片裁剪的支持
    public boolean isCropCircle = false;// 是否圆形裁剪
    public int cropWidth = 500;// 裁剪的宽
    public int cropHeight = 500;// 裁剪的高
    public String cropDirectoryPath;// 裁剪生成的目录
    public int cropQuality = 80;// 裁剪照片的质量

    public PickerConfig() {
    }

    protected PickerConfig(Parcel in) {
        userPickedSet = in.createStringArrayList();
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
        isShowToolbarBehavior = in.readByte() != 0;
        isShowFabBehavior = in.readByte() != 0;
        authority = in.readString();
        isCameraSupport = in.readByte() != 0;
        cameraIconDrawableResId = in.readInt();
        cameraDirectoryPath = in.readString();
        cameraQuality = in.readInt();
        isCropSupport = in.readByte() != 0;
        cropDirectoryPath = in.readString();
        cropQuality = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(userPickedSet);
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
        dest.writeByte((byte) (isShowToolbarBehavior ? 1 : 0));
        dest.writeByte((byte) (isShowFabBehavior ? 1 : 0));
        dest.writeString(authority);
        dest.writeByte((byte) (isCameraSupport ? 1 : 0));
        dest.writeInt(cameraIconDrawableResId);
        dest.writeString(cameraDirectoryPath);
        dest.writeInt(cameraQuality);
        dest.writeByte((byte) (isCropSupport ? 1 : 0));
        dest.writeString(cropDirectoryPath);
        dest.writeInt(cropQuality);
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
}
