package com.frank.lib_picturepicker.picturepicker.mvp.view;

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

import com.bumptech.glide.Glide;
import com.frank.lib_picturepicker.R;
import com.frank.lib_picturepicker.picturepicker.data.PictureFolder;

import java.util.List;

/**
 * Created by think on 2018/5/26.
 * Email: frankchoochina@gmail.com
 * Version: 1.2
 * Description: 用于图片选择的 Dialog
 */
public class PicturePickerDialog {

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

    public PicturePickerDialog(Context context, List<PictureFolder> data) {
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
        mDialog.getWindow().setWindowAnimations(R.style.AnimTranslationVertical);
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

        public DialogItemAdapter(Context context, List<PictureFolder> data, AdapterInteraction listener) {
            this.context = context;
            this.data = data;
            this.callback = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.recycle_item_dialog_picture_picker, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            PictureFolder folder = data.get(holder.getAdapterPosition());
            if (folder.getImagePaths() == null || folder.getImagePaths().isEmpty()) return;
            Glide.with(context).load(folder.getImagePaths().get(0)).into(holder.ivPreview);
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
