package com.frank.picturepicker.support.config;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Frank on 2018/6/21.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 相机拍照的相关参数
 */
public class TakeConfig implements Parcelable {

    public int destQuality = 80;// 拍照后压缩的质量
    public String destFilePath;// 最终的文件路径
    public String authority;// fileProvider 的 authority 属性, 用于 7.0 之后, 查找文件的 URI

    public TakeConfig() {

    }

    protected TakeConfig(Parcel in) {
        destQuality = in.readInt();
        destFilePath = in.readString();
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
        dest.writeInt(destQuality);
        dest.writeString(destFilePath);
        dest.writeString(authority);
    }
}
