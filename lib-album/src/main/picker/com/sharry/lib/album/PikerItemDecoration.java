package com.sharry.lib.album;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019/9/14
 */
class PikerItemDecoration extends RecyclerView.ItemDecoration {

    private final boolean cameraSupport;

    PikerItemDecoration(boolean cameraSupport) {
        this.cameraSupport = cameraSupport;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        // Cache view bounds.
        SharedElementHelper.CACHES.clear();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int adapterPosition = parent.getChildAdapterPosition(child) + (cameraSupport ? -1 : 0);
            SharedElementHelper.CACHES.put(adapterPosition, SharedElementHelper.Bounds.parseFrom(
                    child, adapterPosition));
        }
    }

}
