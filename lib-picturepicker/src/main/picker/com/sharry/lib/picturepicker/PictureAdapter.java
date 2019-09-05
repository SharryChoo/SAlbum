package com.sharry.lib.picturepicker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
    private static final int ITEM_TYPE_CAMERA_HEADER = 347;
    private static final int ITEM_TYPE_VIDEO = 664;

    private final Context mContext;
    private final PickerConfig mConfig;
    private final List<MediaMeta> mDataSet;
    private final List<MediaMeta> mPickedSet;
    private final Interaction mInteraction;
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRefreshIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    PictureAdapter(Context context,
                   PickerConfig config,
                   ArrayList<MediaMeta> dataSet,
                   ArrayList<MediaMeta> pickedSet) {
        if (context instanceof Interaction) {
            this.mInteraction = (Interaction) context;
        } else {
            throw new IllegalArgumentException(context + "must implements " +
                    PictureAdapter.class.getSimpleName() + ".Interaction");
        }
        this.mContext = context;
        this.mConfig = config;
        this.mDataSet = dataSet;
        this.mPickedSet = pickedSet;
    }

    @Override
    public int getItemViewType(int position) {
        if (mConfig.isCameraSupport() && position == 0) {
            return ITEM_TYPE_CAMERA_HEADER;
        }
        int relativePosition = mConfig.isCameraSupport() ? position - 1 : position;
        MediaMeta meta = mDataSet.get(relativePosition);
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
        RecyclerView.ViewHolder vh;
        switch (viewType) {
            case ITEM_TYPE_CAMERA_HEADER:
                vh = new CameraHeaderHolder(parent);
                break;
            case ITEM_TYPE_VIDEO:
                vh = new VideoViewHolder(parent);
                break;
            case ITEM_TYPE_PICTURE:
            default:
                vh = new PictureViewHolder(parent);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CameraHeaderHolder) {
            // nothing.
        } else if (holder instanceof PictureViewHolder) {
            int relativePosition = mConfig.isCameraSupport() ? position - 1 : position;
            if (relativePosition < 0) {
                return;
            }
            final MediaMeta meta = mDataSet.get(relativePosition);
            if (meta == null) {
                return;
            }
            bindPictureItem((PictureViewHolder) holder, meta);
        } else if (holder instanceof VideoViewHolder) {
            int relativePosition = mConfig.isCameraSupport() ? position - 1 : position;
            if (relativePosition < 0) {
                return;
            }
            final MediaMeta meta = mDataSet.get(relativePosition);
            if (meta == null) {
                return;
            }
            bindVideoItem((VideoViewHolder) holder, meta);
        } else {
            // nothing.
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size() + (mConfig.isCropSupport() ? 1 : 0);
    }

    /**
     * 绑定图像视图
     */
    private void bindPictureItem(final PictureViewHolder holder, final MediaMeta meta) {
        holder.ivPicture.setBackgroundColor(mConfig.getPickerItemBackgroundColor());
        holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.ivGifTag.setVisibility(Constants.MIME_TYPE_GIF.equals(meta.mimeType) ? View.VISIBLE : View.GONE);
        PictureLoader.loadPicture(mContext, meta.path, holder.ivPicture);
        // 判断当前 uri 是否被选中了
        final int index = mPickedSet.indexOf(meta);
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
        // 加载视频第一帧
        PictureLoader.loadVideo(mContext, meta.path, meta.thumbnailPath, holder.ivPicture);
        // 判断当前 uri 是否被选中了
        final int index = mPickedSet.indexOf(meta);
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
     * Camera header item view holder.
     */
    class CameraHeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CameraHeaderHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.picture_picker_recycle_item_header_camera,
                    parent,
                    false
            ));
            // 将 ItemView 的高度修正为宽度 parent 的宽度的三分之一
            int itemSize = (parent.getMeasuredWidth() - parent.getPaddingLeft()
                    - parent.getPaddingRight()) / mConfig.getSpanCount();
            ViewGroup.LayoutParams itemParams = itemView.getLayoutParams();
            itemParams.height = itemSize;
            itemView.setLayoutParams(itemParams);
            // 注入点击事件
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mInteraction.onCameraClicked();
        }
    }

    /**
     * Picture item view holder
     */
    class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView ivPicture;
        final CheckedIndicatorView checkIndicator;
        final ImageView ivGifTag;
        final Runnable pictureClickedRunnable = new Runnable() {
            @Override
            public void run() {
                performPictureClicked();
            }
        };

        PictureViewHolder(ViewGroup parent) {
            super(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.picture_picker_recycle_item_picture, parent, false)
            );
            // Initialize ivPicture.
            ivPicture = itemView.findViewById(R.id.iv_picture);
            ivPicture.setOnClickListener(this);
            // Initialize ivGifTag
            ivGifTag = itemView.findViewById(R.id.iv_gif_tag);
            // Initialize checkIndicator.
            checkIndicator = itemView.findViewById(R.id.check_indicator);
            checkIndicator.setTextColor(mConfig.getIndicatorTextColor());
            checkIndicator.setSolidColor(mConfig.getIndicatorSolidColor());
            checkIndicator.setBorderColor(
                    mConfig.getIndicatorBorderCheckedColor(),
                    mConfig.getIndicatorBorderUncheckedColor()
            );
            checkIndicator.setOnClickListener(this);
            adjustItemView(parent);
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

        private void adjustItemView(ViewGroup parent) {
            // 将 ItemView 的高度修正为宽度 parent 的宽度的三分之一
            int itemSize = (parent.getMeasuredWidth() - parent.getPaddingLeft()
                    - parent.getPaddingRight()) / mConfig.getSpanCount();
            ViewGroup.LayoutParams itemParams = itemView.getLayoutParams();
            itemParams.height = itemSize;
            itemView.setLayoutParams(itemParams);
            // 设置指示器的宽高为 ItemView 的五分之一
            int indicatorSize = itemSize / 5;
            ViewGroup.MarginLayoutParams indicatorParams =
                    (ViewGroup.MarginLayoutParams) checkIndicator.getLayoutParams();
            // 动态调整大小
            indicatorParams.width = indicatorSize;
            indicatorParams.height = indicatorSize;
            // 动态调整 Margin
            indicatorParams.rightMargin = indicatorSize / 5;
            indicatorParams.topMargin = indicatorSize / 5;
            checkIndicator.setLayoutParams(indicatorParams);
            // 设置指示器的文本尺寸为指示器宽高的二分之一
            checkIndicator.setTextSize(TypedValue.COMPLEX_UNIT_PX, indicatorSize / 2);
        }

        private void performPictureClicked() {
            int relativePosition = mConfig.isCameraSupport() ? getAdapterPosition() - 1 : getAdapterPosition();
            if (relativePosition < 0) {
                return;
            }
            mInteraction.onPictureClicked(ivPicture, mDataSet.get(relativePosition).path, relativePosition);
        }

        private void performCheckIndicatorClicked() {
            // 获取当前点击图片的 path
            int relativePosition = mConfig.isCameraSupport() ? getAdapterPosition() - 1 : getAdapterPosition();
            if (relativePosition < 0) {
                return;
            }
            MediaMeta meta = mDataSet.get(relativePosition);
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
                checkIndicator.setText(String.valueOf(mPickedSet.size()));
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

        VideoViewHolder(ViewGroup parent) {
            super(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.picture_picker_recycle_item_video,
                            parent,
                            false
                    )
            );
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
            // adjust.
            adjustItemView(parent);
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

        private void adjustItemView(ViewGroup parent) {
            // 将 ItemView 的高度修正为宽度 parent 的宽度的三分之一
            int itemSize = (parent.getMeasuredWidth() - parent.getPaddingLeft()
                    - parent.getPaddingRight()) / mConfig.getSpanCount();
            ViewGroup.LayoutParams itemParams = itemView.getLayoutParams();
            itemParams.height = itemSize;
            itemView.setLayoutParams(itemParams);
            // 设置指示器的宽高为 ItemView 的五分之一
            int indicatorSize = itemSize / 5;
            ViewGroup.MarginLayoutParams indicatorParams =
                    (ViewGroup.MarginLayoutParams) checkIndicator.getLayoutParams();
            // 动态调整大小
            indicatorParams.width = indicatorSize;
            indicatorParams.height = indicatorSize;
            // 动态调整 Margin
            indicatorParams.rightMargin = indicatorSize / 5;
            indicatorParams.topMargin = indicatorSize / 5;
            checkIndicator.setLayoutParams(indicatorParams);
            // 设置指示器的文本尺寸为指示器宽高的二分之一
            checkIndicator.setTextSize(TypedValue.COMPLEX_UNIT_PX, indicatorSize / 2);
        }

        private void performPictureClicked() {
            int relativePosition = mConfig.isCameraSupport() ? getAdapterPosition() - 1 : getAdapterPosition();
            if (relativePosition < 0) {
                return;
            }
            mInteraction.onPictureClicked(ivPicture, mDataSet.get(relativePosition).path, relativePosition);
        }

        private void performCheckIndicatorClicked() {
            // 获取当前点击图片的 path
            int relativePosition = mConfig.isCameraSupport() ? getAdapterPosition() - 1 : getAdapterPosition();
            if (relativePosition < 0) {
                return;
            }
            MediaMeta meta = mDataSet.get(relativePosition);
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
                checkIndicator.setText(String.valueOf(mPickedSet.size()));
            }
        }

    }

    /**
     * Communicate with Activity.
     */
    interface Interaction {

        void onCameraClicked();

        void onPictureClicked(@NonNull ImageView imageView, @NonNull String uri, int position);

        boolean onPictureChecked(@NonNull MediaMeta checkedMeta);

        void onPictureRemoved(@NonNull MediaMeta removedMeta);
    }

}
