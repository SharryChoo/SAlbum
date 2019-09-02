package com.sharry.lib.picturepicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class RecordProgressButton extends View {

    private static final int MSG_WHAT_RECORD_START = 848;

    private Point mCenterPoint = new Point();
    private int mMinimumInnerRadius;
    private int mMaximumInnerRadius;
    private int mMinimumOuterRadius;
    private int mMaximumOuterRadius;

    private Paint mPaint;
    private RectF mRect = new RectF();
    private int mInnerRadius;
    private int mOuterRadius;
    private long mMaxDuration = 100;
    private long mCurDuration = 0;

    private RecordListener mRecordListener;
    private boolean mIsRecording = false;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_RECORD_START:
                    mIsRecording = true;
                    mCurDuration = 0;
                    if (mRecordListener != null) {
                        mRecordListener.onRecordStart();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public RecordProgressButton(Context context) {
        this(context, null);
    }

    public RecordProgressButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordProgressButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int validWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int validHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int validSize = Math.min(validWidth, validHeight);
        // 计算中心位置
        mCenterPoint.x = getPaddingLeft() + validWidth >> 1;
        mCenterPoint.y = getPaddingTop() + validHeight >> 1;
        // 计算环形区域的范围
        mMaximumOuterRadius = validSize >> 1;
        mMinimumOuterRadius = mMaximumOuterRadius * 3 / 4;
        mMaximumInnerRadius = mMinimumOuterRadius * 3 / 4;
        mMinimumInnerRadius = mMaximumInnerRadius / 3;
        // 初始化区域
        mInnerRadius = mMaximumInnerRadius;
        mOuterRadius = mMinimumOuterRadius;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                handleActionUp();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制外部圆环
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.picture_picker_widget_record_progress_outer_corner_gray));
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mOuterRadius, mPaint);
        // 绘制内部圆环
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mInnerRadius, mPaint);
        // 绘制进度
        if (mIsRecording) {
            int strokeWidth = mMinimumInnerRadius >> 1;
            int halfOfStrokeWidth = strokeWidth >> 1;
            // 确定进度的范围
            mRect.top = mCenterPoint.y - mOuterRadius + halfOfStrokeWidth;
            mRect.left = mCenterPoint.x - mOuterRadius + halfOfStrokeWidth;
            mRect.right = mRect.left + (mOuterRadius << 1) - strokeWidth;
            mRect.bottom = mRect.top + (mOuterRadius << 1) - strokeWidth;
            // 配置画笔
            mPaint.setStrokeWidth(strokeWidth);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.picture_picker_base_primary_color));
            canvas.drawArc(mRect, -90, (mCurDuration * 360f / mMaxDuration), false, mPaint);
        }
    }

    /**
     * 设置录制的最大时长
     */
    public void setMaxProgress(long maxDuration) {
        this.mMaxDuration = maxDuration;
    }

    /**
     * 设置当前录制的时长
     */
    public void setCurrentPercent(long curDuration) {
        if (curDuration <= mCurDuration) {
            return;
        }
        this.mCurDuration = curDuration;
        if (mCurDuration >= mMaxDuration) {
            handleActionUp();
        } else {
            postInvalidate();
        }
    }

    /**
     * 设置监听器
     */
    public void setOnRecordListener(RecordListener recordListener) {
        this.mRecordListener = recordListener;
    }

    /**
     * 按下的动画
     */
    private void handleActionDown() {
        // 内圆缩小
        ValueAnimator innerAnimator = ObjectAnimator.ofInt(mInnerRadius, mMinimumInnerRadius)
                .setDuration(200);
        innerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerRadius = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        // 外圆放大
        ValueAnimator outerAnimator = ObjectAnimator.ofInt(mOuterRadius, mMaximumOuterRadius)
                .setDuration(200);
        outerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOuterRadius = (int) animation.getAnimatedValue();
            }
        });
        // 执行动画集合
        AnimatorSet set = new AnimatorSet();
        set.playTogether(innerAnimator, outerAnimator);
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                // 1s 之后视为录制
                mMainHandler.sendMessageDelayed(
                        Message.obtain(mMainHandler, MSG_WHAT_RECORD_START),
                        1000
                );
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mInnerRadius = mMinimumInnerRadius;
                mOuterRadius = mMaximumOuterRadius;
            }
        });
        set.start();
    }

    /**
     * 抬起的动画，
     */
    private void handleActionUp() {
        // 内圆放大
        ValueAnimator innerAnimator = ObjectAnimator.ofInt(mInnerRadius, mMaximumInnerRadius).setDuration(200);
        innerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerRadius = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        // 外圆缩小
        ValueAnimator outerAnimator = ObjectAnimator.ofInt(mOuterRadius, mMinimumOuterRadius).setDuration(200);
        outerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOuterRadius = (int) animation.getAnimatedValue();
            }
        });
        // 执行动画集合
        AnimatorSet set = new AnimatorSet();
        set.playTogether(innerAnimator, outerAnimator);
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mMainHandler.removeMessages(MSG_WHAT_RECORD_START);
                if (mIsRecording) {
                    if (mRecordListener != null) {
                        mRecordListener.onRecordFinish(mCurDuration);
                    }
                } else {
                    if (mRecordListener != null) {
                        mRecordListener.onTakePicture();
                    }
                }
                mIsRecording = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mInnerRadius = mMaximumInnerRadius;
                mOuterRadius = mMinimumOuterRadius;
            }

        });
        set.start();
    }

    public interface RecordListener {

        void onTakePicture();

        void onRecordStart();

        void onRecordFinish(long duration);
    }

}
