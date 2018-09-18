package com.sharry.picturepicker.picturetake.manager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sharry on 2018/6/21.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 相机拍照的相关参数
 */
public class TakeConfig implements Parcelable {

    public String authority;// fileProvider 的 authority 属性, 用于 7.0 之后, 查找文件的 URI
    public int cameraDestQuality = 80;// 拍照后压缩的质量
    public String cameraDirectoryPath;// 存储文件的目录路径
    // 裁剪后的参数
    public boolean isCropSupport = false;// 拍摄后是否开启裁剪
    public boolean isCropCircle = false;// 是否圆形裁剪
    public int cropWidth = 500;// 裁剪的宽
    public int cropHeight = 500;// 裁剪的高
    public int cropQuality = 80;
    public String cropDirectoryPath;// 裁剪文件的目录路径

    public TakeConfig() {

    }

    protected TakeConfig(Parcel in) {
        cameraDestQuality = in.readInt();
        cameraDirectoryPath = in.readString();
        authority = in.readString();
    }

    public static final Creator<TakeConfig> CREATOR = new Creator<TakeConfig>() {
        @Override
        public TakeConfig createFromParcel(Parcel in) {
            return new TakeConfig(in);
        }

        @Override
        public TakeConfig[] newArray(int size) {
            return new TakeConfig[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cameraDestQuality);
        dest.writeString(cameraDirectoryPath);
        dest.writeString(authority);
    }
}
