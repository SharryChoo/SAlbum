package com.sharry.lib.picturepicker;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe pictures that in the same folder.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/31 22:29
 */
class FolderModel {

    private String folderName;
    private List<MediaMeta> metas = new ArrayList<>();

    FolderModel(String folderName) {
        this.folderName = folderName;
    }

    String getFolderName() {
        return folderName;
    }

    List<MediaMeta> getMetas() {
        return metas;
    }

    void addMeta(MediaMeta meta) {
        metas.add(meta);
    }

}