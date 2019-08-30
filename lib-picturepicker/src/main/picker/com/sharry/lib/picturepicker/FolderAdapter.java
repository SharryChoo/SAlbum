package com.sharry.lib.picturepicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 图片文件夹的 Adapter
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/22 15:02
 */
class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    final Context context;
    final List<FolderModel> data;
    final AdapterInteraction callback;

    FolderAdapter(Context context, List<FolderModel> data) {
        if (context instanceof AdapterInteraction) {
            this.callback = (AdapterInteraction) context;
        } else {
            throw new IllegalArgumentException(context + "must implements " +
                    FolderAdapter.class.getSimpleName() + ".AdapterInteraction");
        }
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.picture_picker_recycle_item_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        FolderModel folder = data.get(holder.getAdapterPosition());
        if (folder == null || folder.getPicturePaths() == null || folder.getPicturePaths().isEmpty()) {
            return;
        }
        PictureLoader.load(context, folder.getPicturePaths().get(0), holder.ivPreview);
        holder.tvFolderName.setText(folder.getFolderName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onFolderChecked(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Communicate with Activity.
     */
    public interface AdapterInteraction {

        void onFolderChecked(int position);

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPreview;
        private TextView tvFolderName;

        private ViewHolder(View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tv_folder_name);
            ivPreview = itemView.findViewById(R.id.iv_preview);
        }
    }

}
