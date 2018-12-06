package com.sharry.picturepicker.watcher.impl;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sharry.picturepicker.support.loader.PictureLoader;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 选中视图预览页面的 Adapter
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/22 23:23
 */
class WatcherPreviewAdapter extends RecyclerView.Adapter<WatcherPreviewAdapter.ViewHolder> {

    private final ArrayList<String> userPickedSet;
    private final AdapterInteraction interaction;

    public WatcherPreviewAdapter(ArrayList<String> userPickedSet, AdapterInteraction interaction) {
        this.userPickedSet = userPickedSet;
        this.interaction = interaction;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        int size = parent.getHeight();
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        iv.setPadding(size / 20, size / 20, size / 20, size / 20);
        return new ViewHolder(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        PictureLoader.load(holder.ivPicture.getContext(), userPickedSet.get(position), holder.ivPicture);
    }

    @Override
    public int getItemCount() {
        return userPickedSet.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView ivPicture;

        ViewHolder(View itemView) {
            super(itemView);
            this.ivPicture = (ImageView) itemView;
            ivPicture.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            interaction.onPreviewItemClicked((ImageView) v, userPickedSet.get(position), position);
        }
    }

    public interface AdapterInteraction {

        void onPreviewItemClicked(ImageView imageView, String uri, int position);

    }
}
