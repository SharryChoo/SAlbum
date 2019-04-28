package com.sharry.picturepicker.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * 可拖拽返回的 ViewPager, 这里用作图片查看器
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2017/12/28 16:25
 */
public class DraggableViewPager extends ViewPager {

    private static final int INVALIDATE_VALUE = -1;
    private int mSharedElementPosition = INVALIDATE_VALUE;
    private int mBackgroundColor = INVALIDATE_VALUE;
    private float mFingerUpBackgroundAlpha = 1f;// 手指松开时, 当前ViewPager背景的透明度

    private float mDownX = 0f;
    private float mDownY = 0f;
    private float mCapturedOriginY = 0f;// 被捕获的 View 的 Y 的起始点
    private float mDragThresholdHeight = 0f;// 拖动到可以返回的阈值
    private float mVerticalVelocityThreshold = 1000f;// 竖直方向上速度的阈值

    private boolean mIsDragging = false;
    private boolean mIsAnimRunning = false;

    private VelocityTracker mVelocityTracker;

    public DraggableViewPager(Context context) {
        this(context, null);
    }

    public DraggableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 速度捕获器
        mVelocityTracker = VelocityTracker.obtain();
        // 规定拖拽到消失的阈值
        mDragThresholdHeight = getResources().getDisplayMetrics().heightPixels / 4;
    }

    public interface OnPagerChangedListener {
        void onPagerChanged(int position);
    }

    public void setOnPagerChangedListener(final OnPagerChangedListener listener) {
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (listener != null) {
                    listener.onPagerChanged(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 设置了共享的位置
     */
    public void setSharedElementPosition(int position) {
        this.mSharedElementPosition = position;
    }

    /**
     * 设置背景色
     *
     * @param colorResId 颜色 ID
     */
    public void setBackgroundColorRes(@ColorRes int colorResId) {
        setBackgroundColor(ContextCompat.getColor(getContext(), colorResId));
    }

    @Override
    public void setBackgroundColor(int color) {
        if (mBackgroundColor == INVALIDATE_VALUE) {
            mBackgroundColor = color;
        }
        super.setBackgroundColor(color);
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
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIsDragging || isInvalidateCurrentView()) {
            return super.onTouchEvent(ev);
        }
        mVelocityTracker.addMovement(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCapturedOriginY = getCurrentView().getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = (ev.getRawY() - mDownY) / 5;// 添加阻尼感
                getCurrentView().setY(mCapturedOriginY + deltaY);
                mFingerUpBackgroundAlpha = 1 - (Math.abs(deltaY) / mDragThresholdHeight);
                setBackgroundColor(alphaColor(getBaseColor(), mFingerUpBackgroundAlpha));
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
                mVelocityTracker.recycle();
                mIsDragging = false;
                break;
        }
        return true;
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        super.setAdapter(new DelegatePagerAdapter(adapter));
    }

    /**
     * 获取当前正在展示的 View
     */
    private View getCurrentView() {
        return ((DelegatePagerAdapter) getAdapter()).getCurrentView();
    }

    /**
     * 判断展示的 View 是否可用
     */
    private boolean isInvalidateCurrentView() {
        return getCurrentView() == null;
    }

    /**
     * 恢复到原位
     */
    private void recover() {
        if (isInvalidateCurrentView()) return;
        ValueAnimator recoverAnim = ValueAnimator.ofFloat(getCurrentView().getY(), mCapturedOriginY).setDuration(300);
        recoverAnim.setInterpolator(new OvershootInterpolator(3f));
        recoverAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curY = (float) animation.getAnimatedValue();
                getCurrentView().setY(curY);
                setBackgroundColor(alphaColor(getBaseColor(), mFingerUpBackgroundAlpha
                        + (1 - mFingerUpBackgroundAlpha) * animation.getAnimatedFraction()));
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
        if (isInvalidateCurrentView()) return;
        if (getCurrentItem() == mSharedElementPosition) {
            ((Activity) getContext()).onBackPressed();
            return;
        }
        float destY = (getCurrentView().getY() - mCapturedOriginY > 0 ? 1 : -1)
                * getResources().getDisplayMetrics().heightPixels;
        ValueAnimator dismissAnim = ValueAnimator.ofFloat(getCurrentView().getY(), destY).setDuration(400);
        dismissAnim.setInterpolator(new AnticipateInterpolator(1f));
        dismissAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curY = (float) animation.getAnimatedValue();
                getCurrentView().setY(curY);
                setBackgroundColor(alphaColor(getBaseColor(), mFingerUpBackgroundAlpha * (1 - animation.getAnimatedFraction())));
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
     * 获取用于背景色渐变的基础颜色
     */
    private int getBaseColor() {
        return mBackgroundColor == INVALIDATE_VALUE ? Color.BLACK : mBackgroundColor;
    }

    /**
     * 颜色透明化
     *
     * @param baseColor     需要更改的颜色
     * @param alphaPercent: 0 代表全透明, 1 代表不透明
     */
    private int alphaColor(int baseColor, float alphaPercent) {
        if (alphaPercent > 1) alphaPercent = 1;
        if (alphaPercent < 0) alphaPercent = 0;
        int baseAlpha = (baseColor & 0xff000000) >>> 24;
        int alpha = (int) (baseAlpha * alphaPercent);
        return alpha << 24 | (baseColor & 0xffffff);
    }

    /**
     * 静态代理的 Adapter 对象
     */
    private static final class DelegatePagerAdapter extends PagerAdapter {

        // 记录当前的 View
        private View mCurrentView;
        // 原始的 Adapter
        private PagerAdapter mOriginAdapter;
        // 注册代理监听器
        private DataSetObserver mObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                notifyDataSetChanged();
            }
        };

        DelegatePagerAdapter(PagerAdapter originAdapter) {
            mOriginAdapter = originAdapter;
            mOriginAdapter.registerDataSetObserver(mObserver);
        }

        View getCurrentView() {
            return mCurrentView;
        }

        @Override
        public int getCount() {
            return mOriginAdapter.getCount();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return mOriginAdapter.isViewFromObject(view, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return mOriginAdapter.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mOriginAdapter.destroyItem(container, position, object);
        }

        @Override
        public int getItemPosition(Object object) {
            return mOriginAdapter.getItemPosition(object);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            setPrimaryItem((View) container, position, object);
        }

        @Override
        public void setPrimaryItem(View container, int position, Object object) {
            mCurrentView = (View) object;
        }
    }

}
