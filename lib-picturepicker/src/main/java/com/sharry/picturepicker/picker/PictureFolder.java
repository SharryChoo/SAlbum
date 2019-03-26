package com.sharry.picturepicker.picker;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe pictures that in the same folder.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/31 22:29
 */
class PictureFolder {

    private String folderName;//文件名
    private List<String> picturePaths = new ArrayList<>();//文件夹内部的图片的Uri

    PictureFolder(String folderName) {
        this.folderName = folderName;
    }

    String getFolderName() {
        return folderName;
    }

    List<String> getPicturePaths() {
        return picturePaths;
    }

    void addPath(String imageUri) {
        picturePaths.add(imageUri);
    }

}