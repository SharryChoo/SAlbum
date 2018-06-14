package com.frank.lib_picturepicker.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Frank on 2018/6/14.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 用于展示图片被`选中的索引 View
 */
public class PictureIndicatorView extends AppCompatTextView {

    // Dimension
    private int mBorderWidth;// 边框的宽度
    private int mBorderMargin;// 边框与填充部分的间距
    private int mRadius;// 绘制的半径
    private float mAnimPercent = 0f;

    // Color
    private int mUncheckedBorderColor = Color.WHITE;// 未选中时边框的颜色
    private int mCheckedBorderColor = mUncheckedBorderColor;// 选中时的边框颜色
    private int mSolidColor = Color.BLUE;// 选中时内部填充的颜色

    // Paint
    private Paint mBorderPaint;
    private Paint mSolidPaint;
    private Point mCenterPoint;

    // 用于控制的变量
    private boolean mChecked = false;
    private boolean mIsAnimatorStarted = false;

    public PictureIndicatorView(Context context) {
        this(context, null);
    }

    public PictureIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PictureIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
        init();
    }

    private void init() {
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mUncheckedBorderColor);
        mSolidPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSolidPaint.setColor(mSolidColor);
        mCenterPoint = new Point();
        // 设置一个默认的点击事件
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setChecked(!mChecked);
            }
        });
    }

    public void setChecked(boolean isChecked) {
        if (isChecked != mChecked) {
            if (mChecked) handleCheck2Unchecked();
            else handleUnchecked2Check();
        }
    }

    public boolean isChecked() {
        return mChecked;
    }

    /**
     * 设置边框的颜色
     */
    public void setBorderColor(@ColorInt int checkedColor, @ColorInt int uncheckedColor) {
        mCheckedBorderColor = checkedColor;
        mUncheckedBorderColor = uncheckedColor;
    }

    /**
     * 设置填充的颜色
     */
    public void setSolidColor(@ColorInt int solidColor) {
        mSolidColor = solidColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int validateWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int validateHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        mCenterPoint.x = getPaddingLeft() + validateWidth / 2;
        mCenterPoint.y = getPaddingTop() + validateHeight / 2;
        mRadius = Math.min(validateWidth, validateHeight) / 2;
        mBorderWidth = mRadius / 5;
        mBorderMargin = mBorderWidth / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制外环
        mBorderPaint.setColor(mChecked ? mCheckedBorderColor : mUncheckedBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius - mBorderWidth / 2, mBorderPaint);
        // 绘制内环
        mSolidPaint.setColor(mChecked ? mSolidColor : mUncheckedBorderColor);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mAnimPercent
                * (mRadius - mBorderWidth - mBorderMargin), mSolidPaint);
        if (mChecked) {
            super.onDraw(canvas);
        }
    }

    /**
     * 处理从 选中 到 未选中 状态的改变
     */
    private void handleCheck2Unchecked() {
        if (mIsAnimatorStarted) return;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1, 0).setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimPercent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mChecked = false;
                mIsAnimatorStarted = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimatorStarted = false;
            }
        });
        valueAnimator.setInterpolator(new AnticipateInterpolator(4f));
        valueAnimator.start();
    }

    /**
     * 处理从 未选中 到 选中 状态的改变
     */
    private void handleUnchecked2Check() {
        if (mIsAnimatorStarted) return;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimPercent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mChecked = true;
                mIsAnimatorStarted = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimatorStarted = false;
            }
        });
        valueAnimator.setInterpolator(new OvershootInterpolator(4f));
        valueAnimator.start();
    }

}
