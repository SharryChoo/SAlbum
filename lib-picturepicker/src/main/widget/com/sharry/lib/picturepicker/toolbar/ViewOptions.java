package com.sharry.lib.picturepicker.toolbar;

import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static androidx.annotation.Dimension.PX;

/**
 * Options associated with view.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/28 8:48
 */
public class ViewOptions implements Options<View> {

    /*
      Constants
     */
    static final int DEFAULT_VISIBILITY = View.VISIBLE;
    static final int DEFAULT_WIDTH = ViewGroup.LayoutParams.WRAP_CONTENT;
    static final int DEFAULT_HEIGHT = ViewGroup.LayoutParams.WRAP_CONTENT;
    static final int DEFAULT_PADDING = 0;

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Visibility {
    }

    int visibility = DEFAULT_VISIBILITY;
    // Widget padding
    @Dimension(unit = PX)
    int paddingLeft = DEFAULT_PADDING;
    @Dimension(unit = PX)
    int paddingTop = DEFAULT_PADDING;
    @Dimension(unit = PX)
    int paddingRight = DEFAULT_PADDING;
    @Dimension(unit = PX)
    int paddingBottom = DEFAULT_PADDING;
    // Layout params
    @Dimension(unit = PX)
    int widthExcludePadding = DEFAULT_WIDTH;
    @Dimension(unit = PX)
    int heightExcludePadding = DEFAULT_HEIGHT;
    // listener callback.
    View.OnClickListener listener = null;

    private ViewOptions() {
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    @Override
    public void completion(View view) {
        view.setVisibility(visibility);
        // Set padding.
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
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
    }

    /**
     * Copy values from other instance.
     */
    private void copyFrom(ViewOptions other) {
        this.visibility = other.visibility;
        this.paddingLeft = other.paddingLeft;
        this.paddingTop = other.paddingTop;
        this.paddingRight = other.paddingRight;
        this.paddingBottom = other.paddingBottom;
        this.widthExcludePadding = other.widthExcludePadding;
        this.heightExcludePadding = other.heightExcludePadding;
    }

    /**
     * Builder TextOptions instance more easier.
     */
    public static class Builder {

        private ViewOptions op;

        public Builder() {
            op = new ViewOptions();
        }

        private Builder(@NonNull ViewOptions other) {
            this();
            op.copyFrom(other);
        }

        public Builder setVisibility(@Visibility int visibility) {
            op.visibility = visibility;
            return this;
        }

        public Builder setPaddingLeft(@Dimension(unit = PX) int paddingLeft) {
            op.paddingLeft = paddingLeft;
            return this;
        }

        public Builder setPaddingTop(@Dimension(unit = PX) int paddingTop) {
            op.paddingTop = paddingTop;
            return this;
        }

        public Builder setPaddingRight(@Dimension(unit = PX) int paddingRight) {
            op.paddingRight = paddingRight;
            return this;
        }

        public Builder setPaddingBottom(@Dimension(unit = PX) int paddingBottom) {
            op.paddingBottom = paddingBottom;
            return this;
        }

        public Builder setWidthExcludePadding(@Dimension(unit = PX) int widthExcludePadding) {
            op.widthExcludePadding = widthExcludePadding;
            return this;
        }

        public Builder setHeightExcludePadding(@Dimension(unit = PX) int widthExcludePadding) {
            op.heightExcludePadding = widthExcludePadding;
            return this;
        }

        public Builder setListener(View.OnClickListener listener) {
            op.listener = listener;
            return this;
        }

        public Options build() {
            return op;
        }
    }
}
