package com.sharry.picturepicker.watcher.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 3/15/2019 3:27 PM
 */
public class SharedElementData implements Parcelable {

    static SharedElementData parseFrom(@NonNull View sharedElement, int sharedElementPosition) {
        SharedElementData result = new SharedElementData();
        int[] locations = new int[2];
        sharedElement.getLocationOnScreen(locations);
        result.startX = locations[0];
        result.startY = locations[1];
        result.width = sharedElement.getWidth();
        result.height = sharedElement.getHeight();
        result.sharedPosition = sharedElementPosition;
        return result;
    }

    public int startX;
    public int startY;
    public int width;
    public int height;
    public int sharedPosition;

    private SharedElementData() {

    }

    protected SharedElementData(Parcel in) {
        startX = in.readInt();
        startY = in.readInt();
        width = in.readInt();
        height = in.readInt();
        sharedPosition = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(startX);
        dest.writeInt(startY);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(sharedPosition);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "SharedElementData{" +
                "startX=" + startX +
                ", startY=" + startY +
                ", width=" + width +
                ", height=" + height +
                ", sharedPosition='" + sharedPosition + '\'' +
                '}';
    }

    public static final Creator<SharedElementData> CREATOR = new Creator<SharedElementData>() {
        @Override
        public SharedElementData createFromParcel(Parcel in) {
            return new SharedElementData(in);
        }

        @Override
        public SharedElementData[] newArray(int size) {
            return new SharedElementData[size];
        }
    };

}
