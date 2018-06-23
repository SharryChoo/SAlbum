package com.frank.picturepicker.picturepicker.impl.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.frank.picturepicker.R;
import com.frank.picturepicker.picturepicker.manager.PickerConfig;
import com.frank.picturepicker.support.loader.PictureLoader;
import com.frank.picturepicker.widget.CheckedIndicatorView;

import java.util.List;

/**
 * Created by think on 2018/5/26.
 * Email: frankchoochina@gmail.com
 * Version: 1.2
 * Description: 图片选择器的 Adapter
 */
public class PicturePickerAdapter extends RecyclerView.Adapter<PicturePickerAdapter.ViewHolder> {

    private Context mContext;
    private List<String> mUris;
    private AdapterInteraction mInteraction;
    private PickerConfig mConfig;

    // 用于延时更新角标
    private Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private Runnable mRefreshIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    public interface AdapterInteraction {

        List<String> onUserPickedSet();

        boolean onPictureChecked(String uri);

        void onPictureUnchecked(String uri);

        void onPictureClicked(ImageView imageView, String uri, int position);

        void onCameraClicked();
    }

    public PicturePickerAdapter(Context context, List<String> uris, PickerConfig config) {
        this.mInteraction = (AdapterInteraction) context;
        this.mContext = context;
        this.mUris = uris;
        this.mConfig = config;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.recycle_item_activity_picture_picker, parent, false));
        // 将 ItemView 的高度修正为宽度 parent 的宽度的三分之一
        int itemSize = parent.getMeasuredWidth() / mConfig.spanCount;
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
        holder.ivPicture.setBackgroundColor(mConfig.pickerItemBackgroundColor);
        if (mConfig.isCameraSupport && position == 0) {
            bindCameraHeader(holder);
        } else {
            int relativePosition = position - (mConfig.isCameraSupport ? 1 : 0);
            final String uri = mUris.get(relativePosition);
            bindItemView(holder, uri);
        }
    }

    /**
     * 绑定相机 Header 的数据
     */
    private void bindCameraHeader(ViewHolder holder) {
        holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER);
        holder.ivPicture.setImageResource(mConfig.cameraIconDrawableResId == PickerConfig.INVALIDATE_VALUE
                ? R.drawable.icon_activity_picture_picker_default_camera : mConfig.cameraIconDrawableResId);
        holder.ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteraction.onCameraClicked();
            }
        });
        holder.checkIndicator.setVisibility(View.INVISIBLE);
    }

    /**
     * 绑定普通视图的数据
     */
    private void bindItemView(final ViewHolder holder, final String uri) {
        holder.ivPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        PictureLoader.load(mContext, uri, holder.ivPicture);
        holder.ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteraction.onPictureClicked((ImageView) v, uri, mUris.indexOf(uri));
            }
        });
        // 判断当前 uri 是否被选中了
        final int index = mInteraction.onUserPickedSet().indexOf(uri);
        // 设置点击监听
        holder.checkIndicator.setVisibility(View.VISIBLE);
        holder.checkIndicator.setCheckedWithoutAnimator(index != -1);
        holder.checkIndicator.setText(String.valueOf(index + 1));
        // 设置点击监听器
        holder.checkIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkIndicator.isChecked()) {// Checked-> Unchecked
                    // 移除选中数据与状态
                    mInteraction.onPictureUnchecked(uri);
                    holder.checkIndicator.setChecked(false);
                    // 需要延时的更新索引角标
                    notifyCheckedIndicatorChanged();
                } else {// Unchecked -> Checked
                    // 判断是否达到选择上限
                    holder.checkIndicator.setChecked(mInteraction.onPictureChecked(uri));
                    // 设置文本
                    holder.checkIndicator.setText(String.valueOf(mInteraction.onUserPickedSet().size()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUris.size() + (mConfig.isCameraSupport ? 1 : 0);
    }

    /**
     * 通知选中图片的角标变更
     */
    private void notifyCheckedIndicatorChanged() {
        mMainThreadHandler.postDelayed(mRefreshIndicatorRunnable, 300);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivPicture;
        final CheckedIndicatorView checkIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            ivPicture = itemView.findViewById(R.id.iv_picture);
            checkIndicator = itemView.findViewById(R.id.check_indicator);
            checkIndicator.setTextColor(mConfig.indicatorTextColor);
            checkIndicator.setSolidColor(mConfig.indicatorSolidColor);
            checkIndicator.setBorderColor(mConfig.indicatorBorderCheckedColor, mConfig.indicatorBorderUncheckedColor);
        }
    }

}
