package com.frank.picturepicker.pricturecrop.manager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Frank on 2018/6/21.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 相机拍照的相关参数
 */
public class CropConfig implements Parcelable {

    public String originFilePath;
    public String cropDirectoryPath;
    public boolean isCropCircle;
    public String authority;// fileProvider 的 authority 属性, 用于 7.0 之后, 查找文件的 URI
    public int aspectX = 1;// 方形 X 的比率
    public int aspectY = 1;// 方形 X 的比率
    public int outputX = 500;// 图像输出时的宽
    public int outputY = 500;// 图像输出的高
    public int destQuality = 80;// 裁剪后图片输出的质量

    public CropConfig() {

    }

    protected CropConfig(Parcel in) {
        aspectX = in.readInt();
        aspectY = in.readInt();
        outputX = in.readInt();
        outputY = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(aspectX);
        dest.writeInt(aspectY);
        dest.writeInt(outputX);
        dest.writeInt(outputY);
    }

    @Override
    public int describeContents() {
        return 0;
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
}
