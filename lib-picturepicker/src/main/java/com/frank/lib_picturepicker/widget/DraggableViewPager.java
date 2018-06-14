package com.frank.lib_picturepicker.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by FrankChoo on 2017/12/28.
 * Email: frankchoochina@gmail.com
 * Version:  1.0
 * Description: 可拖拽返回的 ViewPager, 这里用作图片查看器
 */
public class DraggableViewPager extends ViewPager {

    private float mDownX = 0f;
    private float mDownY = 0f;
    private float mDragThresholdHeight = 0f;// 拖动到可以返回的阈值
    private float mVerticalVelocityThreshold = 2000f;// 竖直方向上速度的阈值
    private float mCapturedOriginY = 0f;// 被捕获的 View 的 Y 的起始点
    private float mFingerUpBkgAlpha = 1f;// 手指松开时, 当前ViewPager背景的透明度
    private boolean mIsDragging = false;
    private boolean mIsAnimRunning = false;
    private View mCapturedView;
    private VelocityTracker mVelocityTracker;

    public DraggableViewPager(Context context) {
        this(context, null);
    }

    public DraggableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(Color.BLACK);
        // 速度捕获器
        mVelocityTracker = VelocityTracker.obtain();
        // 规定拖拽到消失的阈值
        mDragThresholdHeight = getResources().getDisplayMetrics().heightPixels / 5;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsAnimRunning) return false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getRawX();
                mDownY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = ev.getRawX() - mDownX;
                float deltaY = ev.getRawY() - mDownY;
                float maxMoved = Math.max(Math.abs(deltaX), Math.abs(deltaY));
                if (maxMoved > ViewConfiguration.get(getContext()).getScaledTouchSlop()
                        && Math.abs(deltaX) < Math.abs(deltaY)) {
                    mIsDragging = true;
                    return true;
                } else {
                    mIsDragging = false;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIsDragging) {
            return super.onTouchEvent(ev);
        }
        mVelocityTracker.addMovement(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCapturedOriginY = mCapturedView.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = (ev.getRawY() - mDownY) / 5;// 添加阻尼感
                mCapturedView.setY(mCapturedOriginY + deltaY);
                mFingerUpBkgAlpha = 1 - (Math.abs(deltaY) / mDragThresholdHeight);
                setBackgroundColor(alphaColor(Color.BLACK, mFingerUpBkgAlpha));
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000);
                // 这里处理拖拽后释放的操作
                if (Math.abs(mVelocityTracker.getYVelocity()) > mVerticalVelocityThreshold
                        || Math.abs(ev.getRawY() - mDownY) > mDragThresholdHeight) {
                    dismiss();
                } else {
                    recover();
                }
                mIsDragging = false;
                mVelocityTracker.recycle();
                break;
        }
        return true;
    }

    /**
     * 恢复到原位
     */
    private void recover() {
        ValueAnimator recoverAnim = ValueAnimator.ofFloat(mCapturedView.getY(), mCapturedOriginY).setDuration(300);
        recoverAnim.setInterpolator(new OvershootInterpolator(3f));
        recoverAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curY = (float) animation.getAnimatedValue();
                mCapturedView.setY(curY);
                setBackgroundColor(alphaColor(Color.BLACK, mFingerUpBkgAlpha
                        + (1 - mFingerUpBkgAlpha) * animation.getAnimatedFraction()));
            }
        });
        recoverAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimRunning = false;
            }
        });
        recoverAnim.start();
    }

    /**
     * 滑动超过阈值时消失
     */
    private void dismiss() {
        float destY = (mCapturedView.getY() - mCapturedOriginY > 0 ? 1 : -1) *
                getResources().getDisplayMetrics().heightPixels;
        ValueAnimator dismissAnim = ValueAnimator.ofFloat(mCapturedView.getY(), destY).setDuration(400);
        dismissAnim.setInterpolator(new AnticipateInterpolator(1f));
        dismissAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curY = (float) animation.getAnimatedValue();
                mCapturedView.setY(curY);
                setBackgroundColor(alphaColor(Color.BLACK, mFingerUpBkgAlpha * (1 - animation.getAnimatedFraction())));
            }
        });
        dismissAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimRunning = false;
                ((Activity) getContext()).onBackPressed();
            }
        });
        dismissAnim.start();
    }

    /**
     * @param baseColor
     * @param alphaPercent: 0 代表全透明, 1 代表不透明
     */
    private int alphaColor(int baseColor, float alphaPercent) {
        if (alphaPercent > 1) alphaPercent = 1;
        if (alphaPercent < 0) alphaPercent = 0;
        int baseAlpha = (baseColor & 0xff000000) >>> 24;
        int alpha = (int) (baseAlpha * alphaPercent);
        return alpha << 24 | (baseColor & 0xffffff);
    }

    public interface OnPagerChangedListener {
        View onPagerChanged(int position);
    }

    public void setOnPagerChangedListener(final OnPagerChangedListener listener) {
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (listener != null) {
                    mCapturedView = listener.onPagerChanged(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
