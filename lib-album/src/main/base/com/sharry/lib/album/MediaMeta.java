package com.sharry.lib.album;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 媒体资源描述
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02 14:01
 */
public class MediaMeta implements Parcelable {

    /**
     * 创建图片资源
     */
    public MediaMeta createPicture(@NonNull Uri uri) {
        return create(uri, "", true);
    }

    /**
     * 创建视频资源
     */
    public MediaMeta createVideo(@NonNull Uri uri) {
        return create(uri, "", false);
    }

    static MediaMeta create(@NonNull Uri uri, String filePath, boolean isPicture) {
        return new MediaMeta(uri, filePath, isPicture);
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

    protected MediaMeta(Parcel in) {
        contentUri = in.readParcelable(Uri.class.getClassLoader());
        path = in.readString();
        isPicture = in.readByte() != 0;
        size = in.readLong();
        date = in.readLong();
        duration = in.readLong();
        thumbnailPath = in.readString();
        mimeType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(contentUri, flags);
        dest.writeString(path);
        dest.writeByte((byte) (isPicture ? 1 : 0));
        dest.writeLong(size);
        dest.writeLong(date);
        dest.writeLong(duration);
        dest.writeString(thumbnailPath);
        dest.writeString(mimeType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 文件的 URI
     * <p>
     * Android 10 以上, 只能够使用 URI 进行文件读写
     */
    @NonNull
    Uri contentUri;

    /**
     * 文件路径
     */
    @Deprecated
    String path;

    /**
     * 判断是否是图片
     */
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
     * <p>
     * Unit ms
     */
    long duration = 0;

    /**
     * 视频缩略图
     */
    @Nullable
    String thumbnailPath;

    /**
     * 媒体文件类型
     */
    String mimeType;

    private MediaMeta(@NonNull Uri uri, @NonNull String filePath, boolean isPicture) {
        this.contentUri = uri;
        this.path = filePath;
        this.isPicture = isPicture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaMeta mediaMeta = (MediaMeta) o;
        return contentUri.equals(mediaMeta.contentUri);
    }

    @Override
    public int hashCode() {
        return contentUri.hashCode();
    }

    @Override
    public String toString() {
        return "MediaMeta{" +
                "contentUri='" + contentUri + '\'' + ", \n" +
                "path='" + path + '\'' + ", \n" +
                "isPicture=" + isPicture + ", \n" +
                "size=" + size + ", \n" +
                "date=" + date + ", \n" +
                "duration=" + duration + ", \n" +
                "thumbnailPath='" + thumbnailPath + '\'' + ", \n" +
                "mimeType='" + mimeType + '\'' + "\n" +
                '}';
    }

    @NonNull
    public Uri getContentUri() {
        return contentUri;
    }

    @NonNull
    @Deprecated
    public String getPath() {
        return path;
    }

    public boolean isPicture() {
        return isPicture;
    }

    public long getSize() {
        return size;
    }

    public long getDate() {
        return date;
    }

    public long getDuration() {
        return duration;
    }

    @Nullable
    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getMimeType() {
        return mimeType;
    }

}
