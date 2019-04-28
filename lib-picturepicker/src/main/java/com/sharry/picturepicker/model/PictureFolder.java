package com.sharry.picturepicker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe pictures that in the same folder.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/31 22:29
 */
public class PictureFolder {

    private String folderName;//文件名
    private List<String> picturePaths = new ArrayList<>();//文件夹内部的图片的Uri

    public PictureFolder(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    public List<String> getPicturePaths() {
        return picturePaths;
    }

    public void addPath(String imageUri) {
        picturePaths.add(imageUri);
    }

}