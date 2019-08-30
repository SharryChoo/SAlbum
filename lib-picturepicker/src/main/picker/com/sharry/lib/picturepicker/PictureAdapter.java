package com.sharry.lib.picturepicker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter associated with PicturePicker.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.3
 * @since 2018/9/1 10:19
 */
class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

    private final Context mContext;
    private final PickerConfig mConfig;
    private final List<String> mDisplayPaths;
    private final List<String> mUserPickedPaths;
    private final AdapterInteraction mInteraction;
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRefreshIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };


    PictureAdapter(Context context, PickerConfig config, List<String> displayPaths, List<String> userPickedPaths) {
        if (context instanceof AdapterInteraction) {
            this.mInteraction = (AdapterInteraction) context;
        } else {
            throw new IllegalArgumentException(context + "must implements " +
                    PictureAdapter.class.getSimpleName() + ".AdapterInteraction");
        }
        this.mContext = context;
        this.mConfig = config;
        this.mDisplayPaths = displayPaths;
        this.mUserPickedPaths = userPickedPaths;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.picture_picker_recycle_item_picture, parent, false));
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
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.ivPicture.setBackgroundColor(mConfig.getPickerItemBackgroundColor());
        if (mConfig.isCameraSupport() && position == 0) {
            bindCameraHeader(holder);
        } else {
            int relativePosition = position - (mConfig.isCameraSupport() ? 1 : 0);
            final String uri = mDisplayPaths.get(relativePosition);
            bindItemView(holder, uri);
        }
    }

    @Override
    public int getItemCount() {
        return mDisplayPaths.size() + (mConfig.isCropSupport() ? 1 : 0);
    }

    /**
     * 绑定相机 Header 的数据
     */
    private void bindCameraHeader(@NonNull ViewHolder holder) {
        holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER);
        holder.ivPicture.setImageResource(R.drawable.picture_picker_picker_camera);
        holder.checkIndicator.setVisibility(View.INVISIBLE);
    }

    /**
     * 绑定普通视图的数据
     */
    private void bindItemView(final ViewHolder holder, final String uri) {
        holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        PictureLoader.load(mContext, uri, holder.ivPicture);
        // 判断当前 uri 是否被选中了
        final int index = mUserPickedPaths.indexOf(uri);
        // 设置点击监听
        holder.checkIndicator.setVisibility(View.VISIBLE);
        holder.checkIndicator.setCheckedWithoutAnimator(index != -1);
        holder.checkIndicator.setText(String.valueOf(index + 1));
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

        boolean onPictureChecked(@NonNull String uri);

        void onPictureRemoved(@NonNull String uri);

        void onPictureClicked(@NonNull ImageView imageView, @NonNull String uri, int position);

        void onCameraClicked();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView ivPicture;
        final CheckedIndicatorView checkIndicator;
        final Runnable pictureClickedRunnable = new Runnable() {
            @Override
            public void run() {
                performPictureClicked();
            }
        };

        ViewHolder(View itemView) {
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
                mInteraction.onPictureClicked(ivPicture, mDisplayPaths.get(position), position);
            }
        }

        private void performCheckIndicatorClicked() {
            // 获取当前点击图片的 path
            int position = getAdapterPosition() - (mConfig.isCameraSupport() ? 1 : 0);
            if (position < 0) {
                return;
            }
            String path = mDisplayPaths.get(position);
            // Checked-> Unchecked
            if (checkIndicator.isChecked()) {
                // 移除选中数据与状态
                mInteraction.onPictureRemoved(path);
                checkIndicator.setChecked(false);
                // 需要延时的更新索引角标
                notifyCheckedIndicatorChanged();
            }
            // Unchecked -> Checked
            else {
                // 判断是否达到选择上限
                checkIndicator.setChecked(mInteraction.onPictureChecked(path));
                // 设置文本
                checkIndicator.setText(String.valueOf(mUserPickedPaths.size()));
            }
        }
    }

}
