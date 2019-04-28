package com.sharry.picturepicker.widget.toolbar;

import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static androidx.annotation.Dimension.PX;
import static androidx.annotation.Dimension.SP;

/**
 * Options associated with TextView.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/28 8:49
 */
public class TextViewOptions implements Options<TextView> {

    /**
     * U can get Builder instance from here.
     */
    public static Builder Builder() {
        return new Builder();
    }

    /*
     Constants
    */
    static final int UN_INITIALIZE_TEXT_SIZE = 0;
    static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    static final int DEFAULT_TITLE_TEXT_SIZE = 18;
    static final int DEFAULT_MENU_TEXT_SIZE = 13;
    static final int DEFAULT_MAX_EMS = 8;
    static final int DEFAULT_LINES = 1;
    static final int DEFAULT_PADDING = 0;
    static final TextUtils.TruncateAt DEFAULT_ELLIPSIZE = TextUtils.TruncateAt.END;

    /*
      Fields
     */
    CharSequence text;
    @Dimension(unit = SP)
    int textSize = UN_INITIALIZE_TEXT_SIZE;
    @ColorInt
    int textColor = DEFAULT_TEXT_COLOR;
    int maxEms = DEFAULT_MAX_EMS;
    int lines = DEFAULT_LINES;
    TextUtils.TruncateAt ellipsize = DEFAULT_ELLIPSIZE;
    // Widget padding
    @Dimension(unit = PX)
    int paddingLeft = DEFAULT_PADDING;
    @Dimension(unit = PX)
    int paddingRight = DEFAULT_PADDING;
    // listener callback.
    View.OnClickListener listener = null;

    private TextViewOptions() {
    }

    /**
     * U can rebuild Options instance from here.
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    @Override
    public void completion(TextView textView) {
        // Set padding.
        textView.setPadding(paddingLeft, 0, paddingRight, 0);
        ViewGroup.LayoutParams params = textView.getLayoutParams();
        if (null == params) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        textView.setLayoutParams(params);
        // Set OnClickListener
        if (null != listener) {
            textView.setOnClickListener(listener);
        }
        // Set some fields associated with this textView.
        textView.setText(text);
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setMaxEms(maxEms);
        textView.setLines(lines);
        textView.setEllipsize(ellipsize);
    }

    /**
     * Copy values from other instance.
     */
    private void copyFrom(@NonNull TextViewOptions other) {
        this.text = other.text;
        this.textSize = other.textSize;
        this.textColor = other.textColor;
        this.maxEms = other.maxEms;
        this.lines = other.lines;
        this.ellipsize = other.ellipsize;
        this.paddingLeft = other.paddingLeft;
        this.paddingRight = other.paddingRight;
        this.listener = other.listener;
    }

    /**
     * Builder TextOptions instance more easier.
     */
    public static class Builder {

        private TextViewOptions op;

        private Builder() {
            op = new TextViewOptions();
        }

        private Builder(@NonNull TextViewOptions other) {
            this();
            op.copyFrom(other);
        }

        public Builder setText(@NonNull CharSequence text) {
            op.text = text;
            return this;
        }

        public Builder setTextSize(@Dimension(unit = SP) int textSize) {
            op.textSize = textSize;
            return this;
        }

        public Builder setTextColor(@ColorInt int textColor) {
            op.textColor = textColor;
            return this;
        }

        public Builder setMaxEms(int maxEms) {
            op.maxEms = maxEms;
            return this;
        }

        public Builder setLines(int lines) {
            op.lines = lines;
            return this;
        }

        public Builder setEllipsize(TextUtils.TruncateAt ellipsize) {
            op.ellipsize = ellipsize;
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

        public Builder setListener(View.OnClickListener listener) {
            op.listener = listener;
            return this;
        }

        public TextViewOptions build() {
            if (null == op.text) {
                throw new UnsupportedOperationException("Please ensure text field nonnull.");
            }
            return op;
        }

    }
}
