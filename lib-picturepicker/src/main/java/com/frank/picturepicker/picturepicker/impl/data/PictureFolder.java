package com.frank.picturepicker.picturepicker.impl.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2018/5/26.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
public class PictureFolder {

    private String mFolderName;//文件名

    private List<String> mImagePaths = new ArrayList<>();//文件夹内部的图片的Uri

    public PictureFolder(String folderName) {
        this.mFolderName = folderName;
    }

    public String getFolderName() {
        return mFolderName;
    }

    public List<String> getImagePaths() {
        return mImagePaths;
    }

    public void addImagePath(String imageUri) {
        mImagePaths.add(imageUri);
    }

}