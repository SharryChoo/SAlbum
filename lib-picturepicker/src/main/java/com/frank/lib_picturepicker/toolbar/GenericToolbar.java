package com.frank.lib_picturepicker.toolbar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Created by FrankChoo on 2017/10/10.
 * Email: frankchoochina@gmail.com
 * Version: 2.8
 * Description:
 * Generic 的最小高度为系统 ActionBar 的高度
 * <p>
 * 1. 可以直接在Xml文件中直接使用
 * 2. 可以使用Builder动态的植入
 */
public class GenericToolbar extends Toolbar {

    private final static int INVALIDATE_VALUE = -1;
    private final static int DEFAULT_COLOR = Color.WHITE;

    // Toolbar中的三个容器
    private LinearLayout mLeftItemContainer;
    private LinearLayout mCenterItemContainer;
    private LinearLayout mRightItemContainer;

    // 提供的标题(文本/图片/自定义)
    private TextView mTitleText;
    private ImageView mTitleImage;
    private int mTextColor = DEFAULT_COLOR;

    // 添加的所有View的缓存, 方便用户通过getViewByTag()找到自己添加的View
    private SparseArray<View> mItemViews = new SparseArray<>();

    public GenericToolbar(Context context) {
        this(context, null);
    }

    public GenericToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GenericToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        removeAllViews();
        // 添加左部容器
        mLeftItemContainer = new LinearLayout(getContext());
        LayoutParams leftParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                getActionBarHeight());
        leftParams.gravity = Gravity.START | Gravity.TOP;
        mLeftItemContainer.setLayoutParams(leftParams);
        mLeftItemContainer.setGravity(Gravity.CENTER_VERTICAL);
        mLeftItemContainer.setPadding((int) dp2px(5), 0, (int) dp2px(5), 0);
        addView(mLeftItemContainer);

        // 添加右部容器
        mRightItemContainer = new LinearLayout(getContext());
        LayoutParams rightParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                getActionBarHeight());
        rightParams.gravity = Gravity.END | Gravity.TOP;
        mRightItemContainer.setLayoutParams(rightParams);
        mRightItemContainer.setGravity(Gravity.CENTER_VERTICAL);
        mRightItemContainer.setPadding((int) dp2px(5), 0, (int) dp2px(5), 0);
        addView(mRightItemContainer);

        // 添加中间容器(最后添加, 它的Gravity不会影响其他位置Child的改变)
        mCenterItemContainer = new LinearLayout(getContext());
        LayoutParams centerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        centerParams.gravity = Gravity.CENTER | Gravity.TOP;
        mCenterItemContainer.setPadding((int) dp2px(5), 0,
                (int) dp2px(5), 0);
        mCenterItemContainer.setLayoutParams(centerParams);
        mCenterItemContainer.setGravity(Gravity.CENTER);
        addView(mCenterItemContainer);
    }

    /**
     * 设置标题的位置
     */
    public void setTitleGravity(int gravity) {
        LayoutParams params = (LayoutParams) mCenterItemContainer.getLayoutParams();
        params.gravity = gravity;
        mCenterItemContainer.setLayoutParams(params);
    }

    /**
     * 设置文本标题
     */
    @Override
    public void setTitle(@StringRes int stringResId) {
        this.setTitle(getResources().getText(stringResId), 18f);
    }

    @Override
    public void setTitle(CharSequence text) {
        this.setTitle(text, 18f);
    }

    public void setTitle(CharSequence text, float textSize) {
        if (mTitleText == null) {
            initTitleText(textSize, DEFAULT_COLOR);
        }
        mTitleText.setText(text);
    }

    public void setTitle(CharSequence text, float textSize, @ColorInt int textColor) {
        if (mTitleText == null) {
            initTitleText(textSize, textColor);
        }
        mTitleText.setText(text);
    }

    public TextView getTitleText() {
        if (mTitleText == null) {
            initTitleText(18f, DEFAULT_COLOR);
        }
        return mTitleText;
    }

    /**
     * 设置标题图片
     */
    public void setTitleImage(@DrawableRes int imageResId) {
        this.setTitleImage(imageResId, INVALIDATE_VALUE, INVALIDATE_VALUE);
    }

    public void setTitleImage(@DrawableRes int imageResId, int width, int height) {
        if (mTitleImage == null) {
            initTitleImage(width, height);
        }
        mTitleImage.setImageResource(imageResId);
    }

    public void setTitleImage(@NonNull TitleImageLoader imageLoader) {
        this.setTitleImage(imageLoader, INVALIDATE_VALUE, INVALIDATE_VALUE);
    }

    public void setTitleImage(@NonNull TitleImageLoader imageLoader, int width, int height) {
        if (mTitleImage == null) {
            initTitleImage(width, height);
        }
        imageLoader.displayImage(getContext(), mTitleImage);
    }

    public ImageView getTitleImage() {
        if (mTitleImage == null) {
            initTitleImage(INVALIDATE_VALUE, INVALIDATE_VALUE);
        }
        return mTitleImage;
    }

    /**
     * 添加用户自定义的标题
     */
    public void addCustomTitle(@NonNull View titleView) {
        mCenterItemContainer.addView(titleView);
    }

    /**
     * 添加左部文本
     */
    public void addLeftText(int tag, CharSequence text, OnClickListener listener) {
        this.addLeftText(tag, text, 14f, listener);
    }

    public void addLeftText(int tag, CharSequence text, /*sp*/float textSize, OnClickListener listener) {
        this.addLeftText(tag, text, textSize, DEFAULT_COLOR, listener);
    }

    public void addLeftText(int tag, CharSequence text, /*sp*/float textSize, @ColorInt int textColor, OnClickListener listener) {
        ensure(tag);
        TextView textView = new TextView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setPadding((int) dp2px(5), 0, (int) dp2px(5), 0);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(mTextColor);
        textView.setText(text);
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setOnClickListener(listener);
        mItemViews.put(tag, textView);
        mLeftItemContainer.addView(textView);
    }

    /**
     * 添加左部图标
     */
    public void addLeftIcon(int tag, @DrawableRes int drawableRes, OnClickListener listener) {
        this.addLeftIcon(tag, drawableRes, INVALIDATE_VALUE, INVALIDATE_VALUE, listener);
    }

    public void addLeftIcon(int tag, @DrawableRes int drawableRes, /*dp*/int width, /*dp*/int height, OnClickListener listener) {
        ensure(tag);
        int destWidth = (width == INVALIDATE_VALUE) ? (int) (getActionBarHeight() * 0.4) : (int) dp2px(width);
        // 增大触控面积
        int verticalPadding = (height == INVALIDATE_VALUE) ? (int) (getActionBarHeight() * 0.3)
                : (getActionBarHeight() - (int) dp2px(height)) / 2;
        ImageView imageView = new ImageView(getContext());
        imageView.setPadding((int) dp2px(5), verticalPadding, (int) dp2px(5), verticalPadding);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                destWidth + imageView.getPaddingRight() + imageView.getPaddingLeft(),
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(params);
        imageView.setImageResource(drawableRes);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setOnClickListener(listener);
        mItemViews.put(tag, imageView);
        mLeftItemContainer.addView(imageView);
    }

    /**
     * 添加左部的 View
     */
    public void addLeftView(int tag, View view, /*dp*/int width, /*dp*/int height, OnClickListener listener) {
        ensure(tag);
        int destWidth = (width == INVALIDATE_VALUE) ? (int) (getActionBarHeight() * 0.4) : (int) dp2px(width);
        // 这样处理是为了增大触控面积
        int verticalPadding = (height == INVALIDATE_VALUE) ? (int) (getActionBarHeight() * 0.3)
                : (getActionBarHeight() - (int) dp2px(height)) / 2;
        view.setPadding((int) dp2px(5), verticalPadding, (int) dp2px(5), verticalPadding);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                destWidth + view.getPaddingRight() + view.getPaddingLeft(),
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        view.setLayoutParams(params);
        view.setOnClickListener(listener);
        mItemViews.put(tag, view);
        mLeftItemContainer.addView(view);
    }

    /**
     * 添加右部文本
     */
    public void addRightText(int tag, CharSequence text, OnClickListener listener) {
        this.addRightText(tag, text, 14f, listener);
    }

    public void addRightText(int tag, CharSequence text, /*sp*/float textSize, OnClickListener listener) {
        this.addRightText(tag, text, textSize, DEFAULT_COLOR, listener);
    }

    public void addRightText(int tag, CharSequence text, /*sp*/float textSize, @ColorInt int textColor, OnClickListener listener) {
        ensure(tag);
        TextView textView = new TextView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setPadding((int) dp2px(5), 0, (int) dp2px(5), 0);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(mTextColor);
        textView.setText(text);
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setOnClickListener(listener);
        mItemViews.put(tag, textView);
        mRightItemContainer.addView(textView);
    }

    /**
     * 添加右部图标
     */
    public void addRightIcon(int tag, @DrawableRes int drawableRes, OnClickListener listener) {
        this.addRightIcon(tag, drawableRes, INVALIDATE_VALUE, INVALIDATE_VALUE, listener);
    }

    public void addRightIcon(int tag, @DrawableRes int drawableRes, /*dp*/int width, /*dp*/int height, OnClickListener listener) {
        ensure(tag);
        int destWidth = (width == INVALIDATE_VALUE) ? (int) (getActionBarHeight() * 0.4) : (int) dp2px(width);
        // 这样处理是为了增大触控面积
        int imageVerticalPadding = (height == INVALIDATE_VALUE) ? (int) (getActionBarHeight() * 0.3)
                : (getActionBarHeight() - (int) dp2px(height)) / 2;
        ImageView imageView = new ImageView(getContext());
        imageView.setPadding((int) dp2px(5), imageVerticalPadding, (int) dp2px(5), imageVerticalPadding);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                destWidth + imageView.getPaddingRight() + imageView.getPaddingLeft(),
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(params);
        imageView.setImageResource(drawableRes);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setOnClickListener(listener);
        mItemViews.put(tag, imageView);
        mRightItemContainer.addView(imageView);
    }

    /**
     * 添加右部的 View
     */
    public void addRightView(int tag, View view, /*dp*/int width, /*dp*/int height, OnClickListener listener) {
        ensure(tag);
        int destWidth = (width == INVALIDATE_VALUE) ? (int) (getActionBarHeight() * 0.4) : (int) dp2px(width);
        // 这样处理是为了增大触控面积
        int verticalPadding = (height == INVALIDATE_VALUE) ? (int) (getActionBarHeight() * 0.3)
                : (getActionBarHeight() - (int) dp2px(height)) / 2;
        view.setPadding((int) dp2px(5), verticalPadding, (int) dp2px(5), verticalPadding);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                destWidth + view.getPaddingRight() + view.getPaddingLeft(),
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        view.setLayoutParams(params);
        view.setOnClickListener(listener);
        mItemViews.put(tag, view);
        mRightItemContainer.addView(view);
    }

    /**
     * 通过Tag获取View
     */
    public <T extends View> T getViewByTag(int tag) {
        return (T) mItemViews.get(tag);
    }

    /**
     * 调整适应沉浸式状态栏
     */
    public void setAdjustToTransparentStatusBar(boolean adjust) {
        if (adjust && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup.LayoutParams params = getLayoutParams();
            if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                // 强制将Toolbar设置为wrap_content, 用来适应padding
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                setLayoutParams(params);
            }
            setPadding(0, getStatusBarHeight(), 0, 0);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() == 3) {
            return;
        }
        super.addView(child, index, params);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 测量完毕后, 判断我们中间标题布局的高度是否小于ActionBar的高度
        if (mCenterItemContainer.getHeight() >= getActionBarHeight()) return;
        LayoutParams params = (LayoutParams) mCenterItemContainer.getLayoutParams();
        params.height = getActionBarHeight();
        mCenterItemContainer.setLayoutParams(params);
    }

    /**
     * 设置背景色
     *
     * @param colorRes color 的 ID
     */
    public void setBackgroundColorRes(@ColorRes int colorRes) {
        setBackgroundColor(ContextCompat.getColor(getContext(), colorRes));
    }

    /**
     * 设置背景图片
     *
     * @param drawableRes Drawable 的 ID
     */
    public void setBackgroundDrawableRes(@DrawableRes int drawableRes) {
        setBackgroundResource(drawableRes);
    }

    /**
     * 图片加载接口, 用户自己实现加载策略
     */
    public interface TitleImageLoader {
        void displayImage(Context context, ImageView titleImage);
    }

    private void initTitleText(float textSize, int textColor) {
        mTitleText = new TextView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int) dp2px(5);
        params.rightMargin = (int) dp2px(5);
        mTitleText.setLayoutParams(params);
        mTitleText.setTextColor(mTextColor);
        mTitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        mTitleText.setMaxEms(8);
        mTitleText.setLines(1);
        mTitleText.setEllipsize(TextUtils.TruncateAt.END);
        mTitleText.setTextColor(textColor);
        mCenterItemContainer.addView(mTitleText);
    }

    private void initTitleImage(int width, int height) {
        mTitleImage = new ImageView(getContext());
        int imageWidth = width == INVALIDATE_VALUE ? (int) (getActionBarHeight() * 0.6) : (int) dp2px(width);
        int imageHeight = height == INVALIDATE_VALUE ? (int) (getActionBarHeight() * 0.6) : (int) dp2px(height);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageWidth, imageHeight);
        params.leftMargin = (int) dp2px(5);
        params.rightMargin = (int) dp2px(5);
        mTitleImage.setLayoutParams(params);
        mCenterItemContainer.addView(mTitleImage);
    }

    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }

    private int getActionBarHeight() {
        TypedValue typedValue = new TypedValue();
        // 将属性解析到TypedValue中
        getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        int actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data,
                getContext().getResources().getDisplayMetrics());
        return actionBarHeight;
    }

    private int getStatusBarHeight() {
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private void ensure(int tag) {
        if (mItemViews.get(tag) != null) {
            throw new IllegalArgumentException("GenericToolbar.ensure --> 请检查给View设置的Tag是否唯一");
        }
    }

    /**
     * 使用Builder动态构建Toolbar, 无需在Xml中写入布局
     * <p>
     * 请确保使用的主题是 NoActionBar
     */
    public static class Builder {

        private Context mContext;
        private GenericToolbar mToolbar;
        private ViewGroup mContentParent;
        private ViewGroup mContentView;
        private Style mStyle = Style.DEFAULT;

        /**
         * 给Activity添加Toolbar
         */
        public Builder(Context context) {
            if (context instanceof Activity) {
                mContext = context;
                // 通过安卓源码中的id拿到mContentParent, 这个就是我们的setContentView的直接父容器
                mContentParent = ((Activity) mContext).findViewById(Window.ID_ANDROID_CONTENT);
                mToolbar = new GenericToolbar(mContext);
            } else {
                throw new IllegalArgumentException("GenericToolbar.Builder.Constructor --> " +
                        "传入的Context不为Activity类型, 或该Context对应的Activity已销毁");
            }
        }

        /**
         * 给View添加Toolbar, 确保传入的View为LinearLayout
         */
        public Builder(View contentView) {
            if (contentView instanceof LinearLayout) {
                mContext = new WeakReference<>(contentView.getContext()).get();
                mToolbar = new GenericToolbar(mContext);
                mContentView = (ViewGroup) contentView;
            } else {
                throw new IllegalArgumentException("GenericToolbar.Builder.Constructor --> " +
                        "传入的View不为LinearLayout, 无法将Toolbar放置正确的位置");
            }
        }

        /**
         * 背景色
         */
        public Builder setBackgroundColor(@ColorInt int color) {
            mToolbar.setBackgroundColor(color);
            return this;
        }

        public Builder setBackgroundColorRes(@ColorRes int colorRes) {
            mToolbar.setBackgroundColorRes(colorRes);
            return this;
        }

        public Builder setBackgroundDrawableRes(@DrawableRes int drawableRes) {
            mToolbar.setBackgroundDrawableRes(drawableRes);
            return this;
        }

        /**
         * 标题位置
         */
        public Builder setTitleGravity(int gravity) {
            mToolbar.setTitleGravity(gravity);
            return this;
        }

        /**
         * 文本标题
         */
        public Builder addTitleText(CharSequence text) {
            mToolbar.setTitle(text);
            return this;
        }

        public Builder addTitleText(CharSequence text, float textSize) {
            mToolbar.setTitle(text, textSize);
            return this;
        }

        public Builder addTitleText(CharSequence text, float textSize, @ColorInt int textColor) {
            mToolbar.setTitle(text, textSize, textColor);
            return this;
        }

        /**
         * 图片标题
         */
        public Builder addTitleImage(@DrawableRes int drawableRes) {
            mToolbar.setTitleImage(drawableRes);
            return this;
        }

        public Builder addTitleImage(@DrawableRes int iconRes, int width, int height) {
            mToolbar.setTitleImage(iconRes, width, height);
            return this;
        }

        public Builder addTitleImage(TitleImageLoader loader) {
            mToolbar.setTitleImage(loader);
            return this;
        }

        public Builder addTitleImage(TitleImageLoader loader, int width, int height) {
            mToolbar.setTitleImage(loader, width, height);
            return this;
        }

        /**
         * 自定义标题
         */
        public Builder addCustomTitle(View titleView) {
            mToolbar.addCustomTitle(titleView);
            return this;
        }

        public Builder addBackIcon(int IconRes) {
            addLeftIcon(0xBBBBBBB, IconRes, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) mContext).finish();
                }
            });
            return this;
        }

        /**
         * 左部图标
         */
        public Builder addLeftIcon(int tag, @DrawableRes int drawableRes, final OnClickListener listener) {
            mToolbar.addLeftIcon(tag, drawableRes, listener);
            return this;
        }

        public Builder addLeftIcon(int tag, @DrawableRes int drawableRes, /*dp*/int width, /*dp*/int height, OnClickListener listener) {
            mToolbar.addLeftIcon(tag, drawableRes, width, height, listener);
            return this;
        }

        /**
         * 左部文本
         */
        public Builder addLeftText(int tag, CharSequence text, final OnClickListener listener) {
            mToolbar.addLeftText(tag, text, listener);
            return this;
        }

        public Builder addLeftText(int tag, CharSequence text, /*sp*/float textSize, OnClickListener listener) {
            mToolbar.addLeftText(tag, text, textSize, listener);
            return this;
        }

        public Builder addLeftText(int tag, CharSequence text,/*sp*/float textSize, @ColorInt int textColor, OnClickListener listener) {
            mToolbar.addLeftText(tag, text, textSize, textColor, listener);
            return this;
        }

        /**
         * 右部图标
         */
        public Builder addRightIcon(int tag, @DrawableRes int drawableRes, final OnClickListener listener) {
            mToolbar.addRightIcon(tag, drawableRes, listener);
            return this;
        }

        public Builder addRightIcon(int tag, @DrawableRes int drawableRes, /*dp*/int width, /*dp*/int height, OnClickListener listener) {
            mToolbar.addRightIcon(tag, drawableRes, width, height, listener);
            return this;
        }

        /**
         * 右部文本
         */
        public Builder addRightText(int tag, CharSequence text, final OnClickListener listener) {
            mToolbar.addRightText(tag, text, listener);
            return this;
        }

        public Builder addRightText(int tag, CharSequence text, /*sp*/float textSize, OnClickListener listener) {
            mToolbar.addRightText(tag, text, textSize, listener);
            return this;
        }

        public Builder addRightText(int tag, CharSequence text,/*sp*/float textSize, @ColorInt int textColor, OnClickListener listener) {
            mToolbar.addRightText(tag, text, textSize, textColor, listener);
            return this;
        }

        public Builder setStatusBarStyle(Style statusBarStyle) {
            if (mContext instanceof Activity) {
                AppBarHelper.with(mContext).setStatusBarStyle(statusBarStyle).apply();
            }
            mStyle = statusBarStyle;
            return this;
        }

        /**
         * 将Toolbar添加到当前Window的DecorView中
         * 调整当前Window中其他View的位置, 以适应Toolbar的插入
         */
        public GenericToolbar apply() {
            // 添加自定义标题的View
            mToolbar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            if (mContentParent != null) {
                mContentParent.addView(mToolbar, 0);
            } else {
                mContentView.addView(mToolbar, 0);
            }
            // 防止用户使用 Builder 模式设置沉浸式状态栏无效
            mToolbar.setAdjustToTransparentStatusBar(isAdjustTransparentStatusBar(mStyle));
            // 等待View的performTraversal完成
            mToolbar.post(new Runnable() {
                @Override
                public void run() {
                    adjustLayout();
                }
            });
            return mToolbar;
        }

        private void adjustLayout() {
            if (mContentParent != null
                    && !(mContentParent instanceof LinearLayout)) {
                // 将我们的主体布局移动到Toolbar的下方
                MarginLayoutParams params = (MarginLayoutParams)
                        mContentParent.getChildAt(1).getLayoutParams();
                params.topMargin += getNeedMarginHeight();
                mContentParent.getChildAt(1).setLayoutParams(params);
            }
        }

        private int getNeedMarginHeight() {
            int toolbarCurHeight = mToolbar.getHeight();
            if (isAdjustTransparentStatusBar(mStyle)) {
                // 若设置了沉浸式状态栏
                // toolbar的高度最小为getStatusBarHeight() + getActionBarHeight()
                if (toolbarCurHeight < getStatusBarHeight() + getActionBarHeight()) {
                    toolbarCurHeight = getStatusBarHeight() + getActionBarHeight();
                }
            }
            return toolbarCurHeight;
        }

        private int getStatusBarHeight() {
            int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return mContext.getResources().getDimensionPixelSize(resourceId);
            }
            return 0;
        }

        private int getActionBarHeight() {
            TypedValue typedValue = new TypedValue();
            // 将属性解析到TypedValue中
            mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data,
                    mContext.getResources().getDisplayMetrics());
            return actionBarHeight;
        }

        /**
         * 根据Style判断是否需要适应沉浸式状态栏
         */
        private boolean isAdjustTransparentStatusBar(Style style) {
            if (style == Style.TRANSLUCENCE || style == Style.TRANSPARENT) {
                return true;
            } else {
                return false;
            }
        }
    }

}
