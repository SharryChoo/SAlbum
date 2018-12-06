package com.sharry.picturepicker.widget.toolbar;

import android.support.annotation.Dimension;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import static android.support.annotation.Dimension.PX;

/**
 * Options associated with ImageView.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/28 8:50
 */
public class ImageViewOptions implements Options<ImageView> {

    /*
      Constants
     */
    static final int UN_INITIALIZE_RES_ID = -1;
    static final ImageView.ScaleType DEFAULT_SCALE_TYPE = ImageView.ScaleType.CENTER_CROP;
    static final int DEFAULT_WIDTH = ViewGroup.LayoutParams.WRAP_CONTENT;
    static final int DEFAULT_Height = ViewGroup.LayoutParams.WRAP_CONTENT;
    static final int DEFAULT_PADDING = 0;
    /*
      Fields associated with image menu.
    */
    @DrawableRes
    int drawableResId = UN_INITIALIZE_RES_ID;
    ImageView.ScaleType scaleType = DEFAULT_SCALE_TYPE;
    // Widget padding
    @Dimension(unit = PX)
    int paddingLeft = DEFAULT_PADDING;
    @Dimension(unit = PX)
    int paddingRight = DEFAULT_PADDING;
    // Layout params
    @Dimension(unit = PX)
    int widthExcludePadding = DEFAULT_WIDTH;
    @Dimension(unit = PX)
    int heightExcludePadding = DEFAULT_Height;
    // listener callback.
    View.OnClickListener listener = null;

    /**
     * U can get Builder instance from here.
     */
    public static Builder Builder() {
        return new Builder();
    }

    private ImageViewOptions() {
    }

    /**
     * U can rebuild Options instance from here.
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    @Override
    public void completion(ImageView view) {
        // Set padding.
        view.setPadding(paddingLeft, 0, paddingRight, 0);
        // Set the layout parameters associated with this textView.
        int validWidth = Utils.isLayoutParamsSpecialValue(widthExcludePadding) ? widthExcludePadding :
                widthExcludePadding + view.getPaddingLeft() + view.getPaddingRight();
        int validHeight = Utils.isLayoutParamsSpecialValue(heightExcludePadding) ? heightExcludePadding :
                heightExcludePadding + view.getPaddingTop() + view.getPaddingBottom();
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (null == params) {
            params = new ViewGroup.LayoutParams(validWidth, validHeight);
        } else {
            params.width = validWidth;
            params.height = validHeight;
        }
        view.setLayoutParams(params);
        // Set OnClickListener
        if (null != listener) {
            view.setOnClickListener(listener);
        }
        // Set some fields associated with this imageView.
        view.setImageResource(drawableResId);
        view.setScaleType(scaleType);
    }

    /**
     * Copy values from other instance.
     */
    private void copyFrom(@NonNull ImageViewOptions other) {
        this.drawableResId = other.drawableResId;
        this.scaleType = other.scaleType;
        this.paddingLeft = other.paddingLeft;
        this.paddingRight = other.paddingRight;
        this.heightExcludePadding = other.heightExcludePadding;
        this.widthExcludePadding = other.widthExcludePadding;
        this.listener = other.listener;
    }

    /**
     * Builder Options instance more easier.
     */
    public static class Builder {

        private ImageViewOptions op;

        private Builder() {
            op = new ImageViewOptions();
        }

        private Builder(@NonNull ImageViewOptions other) {
            this();
            op.copyFrom(other);
        }

        public Builder setDrawableResId(@DrawableRes int drawableResId) {
            op.drawableResId = drawableResId;
            return this;
        }

        public Builder setScaleType(ImageView.ScaleType scaleType) {
            op.scaleType = scaleType;
            return this;
        }

        public Builder setPaddingLeft(@Dimension(unit = PX) int paddingLeft) {
            op.paddingLeft = paddingLeft;
            return this;
        }

        public Builder setPaddingRight(@Dimension(unit = PX) int paddingRight) {
            op.paddingRight = paddingRight;
            return this;
        }

        public Builder setWidthWithoutPadding(@Dimension(unit = PX) int widthExcludePadding) {
            op.widthExcludePadding = widthExcludePadding;
            return this;
        }

        public Builder setHeightWithoutPadding(@Dimension(unit = PX) int heightExcludePadding) {
            op.heightExcludePadding = heightExcludePadding;
            return this;
        }

        public Builder setListener(View.OnClickListener listener) {
            op.listener = listener;
            return this;
        }

        public ImageViewOptions build() {
            return op;
        }

    }
}
