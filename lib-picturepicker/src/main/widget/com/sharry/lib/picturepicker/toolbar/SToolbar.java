package com.sharry.lib.picturepicker.toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharry.lib.picturepicker.R;

import static androidx.annotation.Dimension.DP;
import static androidx.annotation.Dimension.PX;
import static androidx.annotation.Dimension.SP;

/**
 * SToolbar 的最小高度为系统 ActionBar 的高度
 * <p>
 * 1. 可以直接在 Xml 文件中直接使用
 * 2. 可以使用 Builder 动态的植入 {@link Builder}
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 3.2
 * @since 2018/8/27
 */
public class SToolbar extends Toolbar {

    /**
     * Get Builder instance
     * If U want create CommonToolbar dynamic, U should invoke this method.
     */
    public static Builder Builder(Context context) {
        return new Builder(context);
    }

    /**
     * Get Builder instance
     * If U want create CommonToolbar dynamic, U should invoke this method.
     */
    public static Builder Builder(View contentView) {
        return new Builder(contentView);
    }

    private static final int LOCKED_CHILDREN_COUNT = 3;
    private static final int DEFAULT_INTERVAL = 5;

    @Dimension(unit = SP)
    private int mTitleTextSize = TextViewOptions.DEFAULT_TITLE_TEXT_SIZE;
    @Dimension(unit = SP)
    private int mMenuTextSize = TextViewOptions.DEFAULT_MENU_TEXT_SIZE;
    @Dimension(unit = PX)
    private int mMinimumHeight;
    @Dimension(unit = PX)
    private int mSubItemInterval;
    @ColorInt
    private int mTitleTextColor = TextViewOptions.DEFAULT_TEXT_COLOR;
    @ColorInt
    private int mMenuTextColor = TextViewOptions.DEFAULT_TEXT_COLOR;

    // Toolbar support container.
    private LinearLayout mLeftMenuContainer;
    private LinearLayout mCenterContainer;
    private LinearLayout mRightMenuContainer;
    private TextView mTitleText;
    private ImageView mTitleImage;

    public SToolbar(Context context) {
        this(context, null);
    }

    public SToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SToolbar);
        // Initialize arguments.
        initArgs(context, array);
        // Initialize views.
        initViews(context);
        // Set status bar style.
        switch (array.getInt(R.styleable.SToolbar_statusBarStyle, Style.DEFAULT.getVal())) {
            case 0:
                setStatusBarStyle(Style.TRANSPARENT);
                break;
            case 1:
                setStatusBarStyle(Style.TRANSLUCENCE);
                break;
            case 2:
                setStatusBarStyle(Style.HIDE);
                break;
            default:
                break;
        }
        // Set title gravity.
        switch (array.getInt(R.styleable.SToolbar_titleGravity, -1)) {
            case 0:
                setTitleGravity(Gravity.LEFT | Gravity.TOP);
                break;
            case 1:
                setTitleGravity(Gravity.RIGHT | Gravity.TOP);
                break;
            default:
                setTitleGravity(Gravity.CENTER | Gravity.TOP);
                break;
        }
        // Add text title.
        String titleText = array.getString(R.styleable.SToolbar_titleText);
        setTitleText(TextUtils.isEmpty(titleText) ? "" : titleText, mTitleTextSize, mTitleTextColor);
        // Add image title.
        int titleImageResId = array.getResourceId(R.styleable.SToolbar_titleImage, View.NO_ID);
        if (View.NO_ID != titleImageResId) {
            setTitleImage(titleImageResId);
        }
        // Add left menu sub item.
        int backIconResId = array.getResourceId(R.styleable.SToolbar_backIcon, View.NO_ID);
        if (View.NO_ID != backIconResId) {
            addBackIcon(backIconResId);
        }
        int leftMenuIconResId = array.getResourceId(R.styleable.SToolbar_menuLeftIcon, View.NO_ID);
        if (View.NO_ID != leftMenuIconResId) {
            addLeftMenuImage(ImageViewOptions.Builder().setDrawableResId(leftMenuIconResId).build());
        }
        String leftMenuText = array.getString(R.styleable.SToolbar_menuLeftText);
        if (null != leftMenuText) {
            addLeftMenuText(
                    TextViewOptions.Builder()
                            .setText(leftMenuText)
                            .setTextSize(mMenuTextSize)
                            .setTextColor(mMenuTextColor)
                            .build()
            );
        }
        // Add right menu sub item.
        String rightMenuText = array.getString(R.styleable.SToolbar_menuRightText);
        if (null != rightMenuText) {
            addRightMenuText(
                    TextViewOptions.Builder()
                            .setText(rightMenuText)
                            .setTextSize(mMenuTextSize)
                            .setTextColor(mMenuTextColor)
                            .build()
            );
        }
        int rightMenuIconResId = array.getResourceId(R.styleable.SToolbar_menuRightIcon, View.NO_ID);
        if (View.NO_ID != rightMenuIconResId) {
            addRightMenuImage(ImageViewOptions.Builder().setDrawableResId(rightMenuIconResId).build());
        }
        array.recycle();
    }

    private void initArgs(Context context, TypedArray array) {
        mMinimumHeight = array.getDimensionPixelSize(R.styleable.SToolbar_minHeight, Utils.dp2px(context, 56));
        mSubItemInterval = array.getDimensionPixelSize(R.styleable.SToolbar_subItemInterval,
                Utils.dp2px(context, DEFAULT_INTERVAL));
        mTitleTextColor = array.getColor(R.styleable.SToolbar_titleTextColor, mTitleTextColor);
        mTitleTextSize = Utils.px2dp(context, array.getDimensionPixelSize(R.styleable.SToolbar_titleTextSize,
                Utils.dp2px(context, mTitleTextSize)));
        mMenuTextSize = Utils.px2dp(context, array.getDimensionPixelSize(R.styleable.SToolbar_menuTextSize,
                Utils.dp2px(context, mMenuTextSize)));
        mMenuTextColor = array.getColor(R.styleable.SToolbar_menuTextColor, mMenuTextColor);
    }

    private void initViews(Context context) {
        // Set initialize layout params.
        removeAllViews();
        // 1. Add left menu container associated with this toolbar.
        mLeftMenuContainer = new LinearLayout(context);
        LayoutParams leftParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        leftParams.gravity = Gravity.START | Gravity.TOP;
        mLeftMenuContainer.setLayoutParams(leftParams);
        mLeftMenuContainer.setMinimumHeight(mMinimumHeight);
        mLeftMenuContainer.setGravity(Gravity.CENTER_VERTICAL);
        addView(mLeftMenuContainer);
        // 2. Add right menu container associated with this toolbar.
        mRightMenuContainer = new LinearLayout(context);
        LayoutParams rightParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rightParams.gravity = Gravity.END | Gravity.TOP;
        mRightMenuContainer.setLayoutParams(rightParams);
        mRightMenuContainer.setMinimumHeight(mMinimumHeight);
        mRightMenuContainer.setGravity(Gravity.CENTER_VERTICAL);
        addView(mRightMenuContainer);
        // 3. Add center item container associated with this toolbar.
        mCenterContainer = new LinearLayout(context);
        LayoutParams centerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        centerParams.gravity = Gravity.CENTER | Gravity.TOP;
        mCenterContainer.setMinimumHeight(mMinimumHeight);
        mCenterContainer.setPadding(mSubItemInterval, 0, mSubItemInterval, 0);
        mCenterContainer.setLayoutParams(centerParams);
        mCenterContainer.setGravity(Gravity.CENTER_VERTICAL);
        addView(mCenterContainer);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        // Lock heightExcludePadding always is WRAP_CONTENT.
        if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        super.setLayoutParams(params);
    }

    /**
     * Set app bar style associated with this Activity.
     */
    public void setStatusBarStyle(Style style) {
        AppBarHelper.with(getContext()).setStatusBarStyle(style).apply();
        if (Utils.isLollipop() && (style == Style.TRANSPARENT || style == Style.TRANSLUCENCE)) {
            // Setup padding.
            setPadding(getPaddingLeft(), getPaddingTop() + Utils.getStatusBarHeight(getContext()),
                    getPaddingRight(), getPaddingBottom());
        }
    }

    /**
     * Sets the background color to a given resource. The colorResId should refer to
     * a color int.
     */
    public void setBackgroundColorRes(@ColorRes int colorResId) {
        setBackgroundColor(ContextCompat.getColor(getContext(), colorResId));
    }

    /**
     * Set the background to a given resource. The resource should refer to
     * a Drawable object or 0 to remove the background.
     */
    public void setBackgroundDrawableRes(@DrawableRes int drawableRes) {
        setBackgroundResource(drawableRes);
    }

    /**
     * Set gravity for the title associated with these LayoutParams.
     *
     * @see Gravity
     */
    public void setTitleGravity(int gravity) {
        LayoutParams params = (LayoutParams) mCenterContainer.getLayoutParams();
        params.gravity = gravity;
        mCenterContainer.setLayoutParams(params);
    }

    /**
     * Get text title associated with this toolbar.
     */
    public TextView getTitleText() {
        if (null == mTitleText) {
            mTitleText = createTextView();
            addTitleView(mTitleText);
        }
        return mTitleText;
    }

    /**
     * Get image title associated with this toolbar.
     */
    public ImageView getTitleImage() {
        if (null == mTitleImage) {
            mTitleImage = createImageView();
            addTitleView(mTitleImage);
        }
        return mTitleImage;
    }

    /**
     * Set text associated with this toolbar title.
     */
    public void setTitleText(@StringRes int stringResId) {
        this.setTitleText(getResources().getText(stringResId));
    }

    public void setTitleText(@NonNull CharSequence text) {
        this.setTitleText(text, mTitleTextSize);
    }

    public void setTitleText(@NonNull CharSequence text, @Dimension(unit = SP) int textSize) {
        this.setTitleText(text, textSize, mTitleTextColor);
    }

    public void setTitleText(@NonNull CharSequence text, @Dimension(unit = SP) int textSize, @ColorInt int textColor) {
        this.setTitleText(
                TextViewOptions.Builder()
                        .setText(text)
                        .setTextSize(textSize)
                        .setTextColor(textColor)
                        .build()
        );
    }

    public void setTitleText(@NonNull TextViewOptions ops) {
        ops.newBuilder()
                .setTextSize(TextViewOptions.UN_INITIALIZE_TEXT_SIZE != ops.textSize
                        ? ops.textSize : mTitleTextSize)
                .setPaddingLeft(TextViewOptions.DEFAULT_PADDING != ops.paddingLeft
                        ? ops.paddingLeft : mSubItemInterval)
                .setPaddingRight(TextViewOptions.DEFAULT_PADDING != ops.paddingRight
                        ? ops.paddingRight : mSubItemInterval)
                .build()
                .completion(getTitleText());
    }

    /**
     * Set image associated with this toolbar title.
     */
    public void setTitleImage(@DrawableRes int resId) {
        this.setTitleImage(resId, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setTitleImage(@DrawableRes int resId, @Dimension(unit = DP) int width,
                              @Dimension(unit = DP) int height) {
        this.setTitleImage(
                ImageViewOptions.Builder()
                        .setDrawableResId(resId)
                        .setWidthWithoutPadding(width)
                        .setHeightWithoutPadding(height)
                        .build()
        );
    }

    public void setTitleImage(@NonNull ImageViewOptions ops) {
        ops.newBuilder()
                .setPaddingLeft(ImageViewOptions.DEFAULT_PADDING != ops.paddingLeft
                        ? ops.paddingLeft : mSubItemInterval)
                .setPaddingRight(ImageViewOptions.DEFAULT_PADDING != ops.paddingRight
                        ? ops.paddingRight : mSubItemInterval)
                .build()
                .completion(getTitleImage());
    }

    public void addTitleView(@NonNull View view) {
        addTitleView(view, null);
    }

    /**
     * Add custom view associated with this toolbar title.
     * U can set view more easier when U use Options.
     */
    public void addTitleView(@NonNull View view, @Nullable Options ops) {
        if (null != ops) {
            ops.completion(view);
        }
        mCenterContainer.addView(view);
    }

    /**
     * Add back icon associated with this toolbar left menu.
     */
    public void addBackIcon(@DrawableRes int drawableRes) {
        this.addLeftMenuImage(
                ImageViewOptions.Builder()
                        .setDrawableResId(drawableRes)
                        .setListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getContext() instanceof Activity) {
                                    ((Activity) getContext()).onBackPressed();
                                }
                            }
                        })
                        .build()
        );
    }

    /**
     * Add text sub item associated with this toolbar left menu.
     */
    public void addLeftMenuText(@NonNull TextViewOptions ops) {
        addLeftMenuView(createTextView(), ops.newBuilder()
                .setTextSize(TextViewOptions.UN_INITIALIZE_TEXT_SIZE != ops.textSize
                        ? ops.textSize : mMenuTextSize)
                .setPaddingLeft(TextViewOptions.DEFAULT_PADDING != ops.paddingLeft
                        ? ops.paddingLeft : mSubItemInterval)
                .build());
    }

    /**
     * Add image sub item associated with this toolbar left menu.
     */
    public void addLeftMenuImage(@NonNull ImageViewOptions ops) {
        addLeftMenuView(createImageView(), ops.newBuilder()
                .setPaddingLeft(ImageViewOptions.DEFAULT_PADDING != ops.paddingLeft
                        ? ops.paddingLeft : mSubItemInterval)
                .build());
    }

    /**
     * Add custom sub item associated with this toolbar left menu.
     */
    public void addLeftMenuView(@NonNull View view) {
        addLeftMenuView(view, null);
    }

    /**
     * Add custom sub item associated with this toolbar left menu.
     */
    public void addLeftMenuView(@NonNull View view, @Nullable Options ops) {
        if (null != ops) {
            ops.completion(view);
        }
        mLeftMenuContainer.addView(view);
    }

    /**
     * Add text sub item associated with this toolbar right menu.
     */
    public void addRightMenuText(@NonNull TextViewOptions ops) {
        addRightMenuView(createTextView(), ops.newBuilder()
                .setTextSize(TextViewOptions.UN_INITIALIZE_TEXT_SIZE != ops.textSize
                        ? ops.textSize : mMenuTextSize)
                .setPaddingRight(TextViewOptions.DEFAULT_PADDING != ops.paddingRight
                        ? ops.paddingRight : mSubItemInterval)
                .build());
    }

    /**
     * Add image sub item associated with this toolbar right menu.
     */
    public void addRightMenuImage(@NonNull ImageViewOptions ops) {
        addRightMenuView(createImageView(), ops.newBuilder()
                .setPaddingRight(ImageViewOptions.DEFAULT_PADDING != ops.paddingLeft
                        ? ops.paddingLeft : mSubItemInterval)
                .build());
    }

    /**
     * Add custom sub item associated with this toolbar left menu.
     */
    public void addRightMenuView(@NonNull View view) {
        addRightMenuView(view, null);
    }

    /**
     * Add custom sub item associated with this toolbar right menu.
     * U can set view more easier when U use Options.
     */
    public void addRightMenuView(@NonNull View view, @Nullable Options ops) {
        if (null != ops) {
            ops.completion(view);
        }
        mRightMenuContainer.addView(view);
    }

    /**
     * Get view index of left menu.
     */
    public <T extends View> T getLeftMenuView(int index) {
        return (T) mLeftMenuContainer.getChildAt(index);
    }

    /**
     * Get view index of right menu.
     */
    public <T extends View> T getRightMenuView(int index) {
        return (T) mRightMenuContainer.getChildAt(index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (LOCKED_CHILDREN_COUNT == getChildCount()) {
            return;
        }
        super.addView(child, index, params);
    }

    @Override
    public void setMinimumHeight(int minimumHeight) {
        mMinimumHeight = minimumHeight;
        // Reset container minimumHeight
        mLeftMenuContainer.setMinimumHeight(mMinimumHeight);
        mRightMenuContainer.setMinimumHeight(mMinimumHeight);
        mCenterContainer.setMinimumHeight(mMinimumHeight);
    }

    /**
     * Set item horizontal interval associated with this toolbar.
     */
    void setSubItemInterval(int subItemInterval) {
        mSubItemInterval = subItemInterval;
    }

    /**
     * Get TextView instance.
     */
    private TextView createTextView() {
        TextView textView = new TextView(getContext());
        // Set params for the view.
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    /**
     * Get ImageView instance.
     */
    private ImageView createImageView() {
        // Create ImageView instance.
        ImageView imageView = new ImageView(getContext());
        // Set default layout params.
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        imageView.setLayoutParams(params);
        return imageView;
    }

}
