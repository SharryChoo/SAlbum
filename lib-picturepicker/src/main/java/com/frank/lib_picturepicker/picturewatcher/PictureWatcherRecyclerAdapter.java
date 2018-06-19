package com.frank.lib_picturepicker.picturewatcher;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.frank.lib_picturepicker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 2018/6/19.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
public class PictureWatcherRecyclerAdapter extends RecyclerView.Adapter<PictureWatcherRecyclerAdapter.ViewHolder> {

    final ArrayList<String> userPickedSet;
    final AdapterInteraction interaction;

    public PictureWatcherRecyclerAdapter(ArrayList<String> userPickedSet, AdapterInteraction interaction) {
        this.userPickedSet = userPickedSet;
        this.interaction = interaction;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.recycle_item_activity_picture_watcher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Glide.with(holder.ivPicture).load(userPickedSet.get(position)).into(holder.ivPicture);
        holder.ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                interaction.onItemClicked((ImageView) v, userPickedSet.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userPickedSet.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final FrameLayout flContainer;
        final ImageView ivPicture;

        ViewHolder(View itemView) {
            super(itemView);
            this.flContainer = itemView.findViewById(R.id.container);
            this.ivPicture = itemView.findViewById(R.id.iv_picture);
        }
    }

    public interface AdapterInteraction {

        void onItemClicked(ImageView imageView, String uri, int position);

    }
}
