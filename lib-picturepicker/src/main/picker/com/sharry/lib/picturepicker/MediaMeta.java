package com.sharry.lib.picturepicker;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02 14:01
 */
public class MediaMeta implements Parcelable {

    static MediaMeta create(@NonNull String path, boolean isPicture) {
        return new MediaMeta(path, isPicture);
    }

    /**
     * 文件路径
     */
    @NonNull
    final String path;

    final boolean isPicture;

    /**
     * 文件大小
     */
    long size = 0;

    /**
     * 文件创建时间
     */
    long date = 0;

    /**
     * 时长(视频)
     */
    long duration = 0;

    /**
     * 视频缩略图
     */
    String thumbNailPath;

    /**
     * 媒体文件类型
     */
    String mimeType;

    private MediaMeta(@NonNull String path, boolean isPicture) {
        this.path = path;
        this.isPicture = isPicture;
    }

    MediaMeta(Parcel in) {
        path = in.readString();
        isPicture = in.readByte() != 0;
        size = in.readLong();
        date = in.readLong();
        duration = in.readLong();
        mimeType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeByte((byte) (isPicture ? 1 : 0));
        dest.writeLong(size);
        dest.writeLong(date);
        dest.writeLong(duration);
        dest.writeString(mimeType);
    }

    public static final Creator<MediaMeta> CREATOR = new Creator<MediaMeta>() {
        @Override
        public MediaMeta createFromParcel(Parcel in) {
            return new MediaMeta(in);
        }

        @Override
        public MediaMeta[] newArray(int size) {
            return new MediaMeta[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaMeta mediaMeta = (MediaMeta) o;
        return path.equals(mediaMeta.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
