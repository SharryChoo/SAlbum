package com.frank.picturepicker.picturewatcher.impl;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.frank.picturepicker.support.loader.PictureLoader;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/19.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
class PictureWatcherPreviewAdapter extends RecyclerView.Adapter<PictureWatcherPreviewAdapter.ViewHolder> {

    private final ArrayList<String> userPickedSet;
    private final AdapterInteraction interaction;

    public PictureWatcherPreviewAdapter(ArrayList<String> userPickedSet, AdapterInteraction interaction) {
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
        holder.ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                interaction.onPreviewItemClicked((ImageView) v, userPickedSet.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userPickedSet.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivPicture;

        ViewHolder(View itemView) {
            super(itemView);
            this.ivPicture = (ImageView) itemView;
        }
    }

    public interface AdapterInteraction {

        void onPreviewItemClicked(ImageView imageView, String uri, int position);

    }
}
