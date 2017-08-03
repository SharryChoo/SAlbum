package frank.demo.photopicker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 99538 on 2017/7/25.
 */
public class PictureFolderModel {
    private String folderName;//文件名
    private List<String> imageUriList = new ArrayList<>();//文件夹内部的图片的Uri

    public PictureFolderModel(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public List<String> getImageUriList() {
        return imageUriList;
    }

    public void addImageUriList(String imageUri) {
        imageUriList.add(imageUri);
    }

}
