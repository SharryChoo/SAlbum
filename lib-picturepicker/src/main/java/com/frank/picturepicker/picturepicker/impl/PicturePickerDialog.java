package com.frank.picturepicker.picturepicker.impl;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.frank.picturepicker.R;
import com.frank.picturepicker.support.loader.PictureLoader;

import java.util.List;

/**
 * Dialog associated with picture picker.
 *
 * @author Frank <a href="frankchoochina@gmail.com">Contact me.</a>
 * @version 1.3
 * @since 2018/9/1 10:19
 */
class PicturePickerDialog {

    /**
     * Get instance of PicturePickerDialog
     */
    public static PicturePickerDialog with(Context context, List<PictureFolder> data) {
        return new PicturePickerDialog(context, data);
    }

    private Dialog mDialog;
    private OnItemClickedListener mExternalListener;
    private AdapterInteraction mInternalListener = new AdapterInteraction() {
        @Override
        public void onClicked(int position) {
            if (mExternalListener != null) {
                mExternalListener.onDialogItemClicked(position);
            }
            mDialog.dismiss();
        }
    };

    private PicturePickerDialog(Context context, List<PictureFolder> data) {
        // 初始化控件
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new DialogItemAdapter(context, data, mInternalListener));
        // 创建 Dialog
        mDialog = new Dialog(context);
        mDialog.setContentView(recyclerView);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.getWindow().setWindowAnimations(R.style.PicturePickerBottomMenuDialogAnimation);
        mDialog.getWindow().setGravity(Gravity.BOTTOM | Gravity.CENTER);
        // 设置 Window 宽高
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = context.getResources().getDisplayMetrics().widthPixels;
        params.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.6);
        mDialog.getWindow().setAttributes(params);
    }

    public PicturePickerDialog setOnItemClickedListener(OnItemClickedListener listener) {
        mExternalListener = listener;
        return this;
    }

    public PicturePickerDialog show() {
        mDialog.show();
        return this;
    }

    public interface AdapterInteraction {
        void onClicked(int position);
    }

    public interface OnItemClickedListener {
        void onDialogItemClicked(int position);
    }

    /**
     * Dialog 中 RecyclerView 的适配器
     */
    private static class DialogItemAdapter extends RecyclerView.Adapter<DialogItemAdapter.ViewHolder> {

        final Context context;
        final List<PictureFolder> data;
        final AdapterInteraction callback;

        DialogItemAdapter(Context context, List<PictureFolder> data, AdapterInteraction listener) {
            this.context = context;
            this.data = data;
            this.callback = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.libpicturepicker_recycle_item_dialog_picture_picker, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            PictureFolder folder = data.get(holder.getAdapterPosition());
            if (folder.getPicturePaths() == null || folder.getPicturePaths().isEmpty()) return;
            PictureLoader.load(context, folder.getPicturePaths().get(0), holder.ivPreview);
            holder.tvFolderName.setText(folder.getFolderName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onClicked(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivPreview;
            private TextView tvFolderName;

            public ViewHolder(View itemView) {
                super(itemView);
                tvFolderName = itemView.findViewById(R.id.tv_folder_name);
                ivPreview = itemView.findViewById(R.id.iv_preview);
            }
        }

    }
}
