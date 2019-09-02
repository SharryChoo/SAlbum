package com.sharry.lib.picturepicker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter associated with PicturePicker.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.3
 * @since 2018/9/1 10:19
 */
class PictureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_PICTURE = 838;
    private static final int ITEM_TYPE_GIF = 347;
    private static final int ITEM_TYPE_VIDEO = 664;

    private final Context mContext;
    private final PickerConfig mConfig;
    private final List<MediaMeta> mDisplayMetas;
    private final List<MediaMeta> mUserPickedPaths;
    private final AdapterInteraction mInteraction;
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRefreshIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    PictureAdapter(Context context, PickerConfig config, ArrayList<MediaMeta> metas, ArrayList<MediaMeta> userPickedPaths) {
        if (context instanceof AdapterInteraction) {
            this.mInteraction = (AdapterInteraction) context;
        } else {
            throw new IllegalArgumentException(context + "must implements " +
                    PictureAdapter.class.getSimpleName() + ".AdapterInteraction");
        }
        this.mContext = context;
        this.mConfig = config;
        this.mDisplayMetas = metas;
        this.mUserPickedPaths = userPickedPaths;
    }

    @Override
    public int getItemViewType(int position) {
        if (mConfig.isCameraSupport() && position == 0) {
            return ITEM_TYPE_PICTURE;
        }
        int relativePosition = position - (mConfig.isCameraSupport() ? 1 : 0);
        MediaMeta meta = mDisplayMetas.get(relativePosition);
        int result;
        if (meta == null || meta.isPicture) {
            result = ITEM_TYPE_PICTURE;
        } else {
            result = ITEM_TYPE_VIDEO;
        }
        return result;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_VIDEO:
                VideoViewHolder videoViewHolder = new VideoViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.picture_picker_recycle_item_video,
                                parent,
                                false
                        )
                );
                setupVideoViewHolder(parent, videoViewHolder);
                return videoViewHolder;
            case ITEM_TYPE_PICTURE:
            default:
                PictureViewHolder pictureViewHolder = new PictureViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.picture_picker_recycle_item_picture,
                                parent,
                                false
                        )
                );
                setupPictureViewHolder(parent, pictureViewHolder);
                return pictureViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (mConfig.isCameraSupport() && position == 0) {
            bindCameraHeader((PictureViewHolder) holder);
        } else {
            int relativePosition = position - (mConfig.isCameraSupport() ? 1 : 0);
            if (relativePosition < 0) {
                return;
            }
            final MediaMeta meta = mDisplayMetas.get(relativePosition);
            if (meta == null) {
                return;
            }
            if (meta.isPicture) {
                bindPictureItem((PictureViewHolder) holder, meta);
            } else {
                bindVideoItem((VideoViewHolder) holder, meta);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDisplayMetas.size() + (mConfig.isCropSupport() ? 1 : 0);
    }

    private void setupPictureViewHolder(ViewGroup parent, PictureViewHolder holder) {
        // 将 ItemView 的高度修正为宽度 parent 的宽度的三分之一
        int itemSize = (parent.getMeasuredWidth() - parent.getPaddingLeft()
                - parent.getPaddingRight()) / mConfig.getSpanCount();
        ViewGroup.LayoutParams itemParams = holder.itemView.getLayoutParams();
        itemParams.height = itemSize;
        holder.itemView.setLayoutParams(itemParams);
        // 设置指示器的宽高为 ItemView 的五分之一
        int indicatorSize = itemSize / 5;
        ViewGroup.MarginLayoutParams indicatorParams =
                (ViewGroup.MarginLayoutParams) holder.checkIndicator.getLayoutParams();
        // 动态调整大小
        indicatorParams.width = indicatorSize;
        indicatorParams.height = indicatorSize;
        // 动态调整 Margin
        indicatorParams.rightMargin = indicatorSize / 5;
        indicatorParams.topMargin = indicatorSize / 5;
        holder.checkIndicator.setLayoutParams(indicatorParams);
        // 设置指示器的文本尺寸为指示器宽高的二分之一
        holder.checkIndicator.setTextSize(TypedValue.COMPLEX_UNIT_PX, indicatorSize / 2);
    }

    private void setupVideoViewHolder(ViewGroup parent, VideoViewHolder holder) {
        // 将 ItemView 的高度修正为宽度 parent 的宽度的三分之一
        int itemSize = (parent.getMeasuredWidth() - parent.getPaddingLeft()
                - parent.getPaddingRight()) / mConfig.getSpanCount();
        ViewGroup.LayoutParams itemParams = holder.itemView.getLayoutParams();
        itemParams.height = itemSize;
        holder.itemView.setLayoutParams(itemParams);
        // 设置指示器的宽高为 ItemView 的五分之一
        int indicatorSize = itemSize / 5;
        ViewGroup.MarginLayoutParams indicatorParams =
                (ViewGroup.MarginLayoutParams) holder.checkIndicator.getLayoutParams();
        // 动态调整大小
        indicatorParams.width = indicatorSize;
        indicatorParams.height = indicatorSize;
        // 动态调整 Margin
        indicatorParams.rightMargin = indicatorSize / 5;
        indicatorParams.topMargin = indicatorSize / 5;
        holder.checkIndicator.setLayoutParams(indicatorParams);
        // 设置指示器的文本尺寸为指示器宽高的二分之一
        holder.checkIndicator.setTextSize(TypedValue.COMPLEX_UNIT_PX, indicatorSize / 2);
    }

    /**
     * 绑定相机 Header 的数据
     */
    private void bindCameraHeader(@NonNull PictureViewHolder holder) {
        holder.ivPicture.setBackgroundColor(mConfig.getPickerItemBackgroundColor());
        holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER);
        holder.ivPicture.setImageResource(R.drawable.ic_picture_picker_picker_camera_header);
        holder.checkIndicator.setVisibility(View.INVISIBLE);
    }

    /**
     * 绑定图像视图
     */
    private void bindPictureItem(final PictureViewHolder holder, final MediaMeta meta) {
        holder.ivPicture.setBackgroundColor(mConfig.getPickerItemBackgroundColor());
        holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        PictureLoader.load(mContext, meta.path, holder.ivPicture);
        // 判断当前 uri 是否被选中了
        final int index = mUserPickedPaths.indexOf(meta);
        // 设置点击监听
        holder.checkIndicator.setVisibility(View.VISIBLE);
        holder.checkIndicator.setCheckedWithoutAnimator(index != -1);
        holder.checkIndicator.setText(String.valueOf(index + 1));
    }

    /**
     * 绑定视频视图
     */
    private void bindVideoItem(final VideoViewHolder holder, final MediaMeta meta) {
        holder.ivPicture.setBackgroundColor(mConfig.getPickerItemBackgroundColor());
        holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        // 展示视频缩略图
        if (TextUtils.isEmpty(meta.thumbnailPath)) {
            holder.ivPicture.setImageResource(R.drawable.ic_picture_picker_picker_video_default);
        } else {
            assert meta.thumbnailPath != null;
            PictureLoader.load(mContext, meta.thumbnailPath, holder.ivPicture);
        }
        // 判断当前 uri 是否被选中了
        final int index = mUserPickedPaths.indexOf(meta);
        // 设置点击监听
        holder.checkIndicator.setVisibility(View.VISIBLE);
        holder.checkIndicator.setCheckedWithoutAnimator(index != -1);
        holder.checkIndicator.setText(String.valueOf(index + 1));
        // 设置时长
        holder.tvDuration.setText(DateUtil.format(meta.duration));
    }

    /**
     * 通知选中图片的角标变更
     */
    private void notifyCheckedIndicatorChanged() {
        mMainThreadHandler.postDelayed(mRefreshIndicatorRunnable, 300);
    }

    /**
     * Communicate with Activity.
     */
    interface AdapterInteraction {

        boolean onPictureChecked(@NonNull MediaMeta checkedMeta);

        void onPictureRemoved(@NonNull MediaMeta removedMeta);

        void onPictureClicked(@NonNull ImageView imageView, @NonNull String uri, int position);

        void onCameraClicked();
    }

    /**
     * Picture item view holder
     */
    class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView ivPicture;
        final CheckedIndicatorView checkIndicator;
        final Runnable pictureClickedRunnable = new Runnable() {
            @Override
            public void run() {
                performPictureClicked();
            }
        };

        PictureViewHolder(View itemView) {
            super(itemView);
            // Initialize ivPicture.
            ivPicture = itemView.findViewById(R.id.iv_picture);
            ivPicture.setOnClickListener(this);
            // Initialize checkIndicator.
            checkIndicator = itemView.findViewById(R.id.check_indicator);
            checkIndicator.setTextColor(mConfig.getIndicatorTextColor());
            checkIndicator.setSolidColor(mConfig.getIndicatorSolidColor());
            checkIndicator.setBorderColor(
                    mConfig.getIndicatorBorderCheckedColor(),
                    mConfig.getIndicatorBorderUncheckedColor()
            );
            checkIndicator.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ivPicture == v) {
                // 延时 100 mm , 等待水波纹动画结束
                mMainThreadHandler.postDelayed(pictureClickedRunnable, 100);
            } else if (checkIndicator == v) {
                performCheckIndicatorClicked();
            }
        }

        private void performPictureClicked() {
            // 单独处理相机的点击事件
            if (mConfig.isCameraSupport() && 0 == getAdapterPosition()) {
                mInteraction.onCameraClicked();
            } else {
                int position = getAdapterPosition() - (mConfig.isCameraSupport() ? 1 : 0);
                if (position < 0) {
                    return;
                }
                mInteraction.onPictureClicked(ivPicture, mDisplayMetas.get(position).path, position);
            }
        }

        private void performCheckIndicatorClicked() {
            // 获取当前点击图片的 path
            int position = getAdapterPosition() - (mConfig.isCameraSupport() ? 1 : 0);
            if (position < 0) {
                return;
            }
            MediaMeta meta = mDisplayMetas.get(position);
            // Checked-> Unchecked
            if (checkIndicator.isChecked()) {
                // 移除选中数据与状态
                mInteraction.onPictureRemoved(meta);
                checkIndicator.setChecked(false);
                // 需要延时的更新索引角标
                notifyCheckedIndicatorChanged();
            }
            // Unchecked -> Checked
            else {
                // 判断是否达到选择上限
                checkIndicator.setChecked(mInteraction.onPictureChecked(meta));
                // 设置文本
                checkIndicator.setText(String.valueOf(mUserPickedPaths.size()));
            }
        }
    }

    /**
     * Video item view holder
     */
    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView ivPicture;
        final CheckedIndicatorView checkIndicator;
        final TextView tvDuration;
        final Runnable pictureClickedRunnable = new Runnable() {
            @Override
            public void run() {
                performPictureClicked();
            }
        };

        VideoViewHolder(View itemView) {
            super(itemView);
            // Initialize ivPicture.
            ivPicture = itemView.findViewById(R.id.iv_picture);
            ivPicture.setOnClickListener(this);
            // Initialize checkIndicator.
            checkIndicator = itemView.findViewById(R.id.check_indicator);
            checkIndicator.setTextColor(mConfig.getIndicatorTextColor());
            checkIndicator.setSolidColor(mConfig.getIndicatorSolidColor());
            checkIndicator.setBorderColor(
                    mConfig.getIndicatorBorderCheckedColor(),
                    mConfig.getIndicatorBorderUncheckedColor()
            );
            checkIndicator.setOnClickListener(this);
            // Initialize tvDuration
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }

        @Override
        public void onClick(View v) {
            if (ivPicture == v) {
                // 延时 100 mm , 等待水波纹动画结束
                mMainThreadHandler.postDelayed(pictureClickedRunnable, 100);
            } else if (checkIndicator == v) {
                performCheckIndicatorClicked();
            }
        }

        private void performPictureClicked() {
            // 单独处理相机的点击事件
            if (mConfig.isCameraSupport() && 0 == getAdapterPosition()) {
                mInteraction.onCameraClicked();
            } else {
                int position = getAdapterPosition() - (mConfig.isCameraSupport() ? 1 : 0);
                if (position < 0) {
                    return;
                }
                mInteraction.onPictureClicked(ivPicture, mDisplayMetas.get(position).path, position);
            }
        }

        private void performCheckIndicatorClicked() {
            // 获取当前点击图片的 path
            int position = getAdapterPosition() - (mConfig.isCameraSupport() ? 1 : 0);
            if (position < 0) {
                return;
            }
            MediaMeta meta = mDisplayMetas.get(position);
            // Checked-> Unchecked
            if (checkIndicator.isChecked()) {
                // 移除选中数据与状态
                mInteraction.onPictureRemoved(meta);
                checkIndicator.setChecked(false);
                // 需要延时的更新索引角标
                notifyCheckedIndicatorChanged();
            }
            // Unchecked -> Checked
            else {
                // 判断是否达到选择上限
                checkIndicator.setChecked(mInteraction.onPictureChecked(meta));
                // 设置文本
                checkIndicator.setText(String.valueOf(mUserPickedPaths.size()));
            }
        }
    }

}
