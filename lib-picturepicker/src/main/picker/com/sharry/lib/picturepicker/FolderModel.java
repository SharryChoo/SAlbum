package com.sharry.lib.picturepicker;

import androidx.annotation.NonNull;

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

    private final String name;
    private final List<MediaMeta> metas = new ArrayList<>();

    FolderModel(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    List<MediaMeta> getMetas() {
        return metas;
    }

    void addMeta(@NonNull MediaMeta meta) {
        int insertIndex = 0;
        for (; insertIndex < metas.size(); insertIndex++) {
            if (metas.get(insertIndex).date < meta.date) {
                break;
            }
        }
        metas.add(insertIndex, meta);
    }

}