package com.sharry.lib.album;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * 可拖拽返回的 ViewPager, 这里用作图片查看器
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.1
 * @since 2017/12/28 16:25
 */
public class DraggableViewPager extends ViewPager {

    private static final int INVALIDATE_VALUE = -1;

    private int mBaseColor = Color.BLACK;

    private float mDownX = 0f;
    private float mDownY = 0f;
    private float mCapturedOriginY = 0f;
    private float mDragThresholdHeight = 0f;
    private float mVerticalVelocityThreshold = 1000f;
    private float mFingerUpBackgroundAlpha = 1f;

    private boolean mIsDragging = false;
    private boolean mIsAnimRunning = false;

    private VelocityTracker mVelocityTracker;
    private Callback mCallback;

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
        // set page change listener
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mCallback != null) {
                    mCallback.onPagerChanged(position);
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
     * 设置拖拽消失监听
     */
    public void setCallback(@Nullable Callback listener) {
        this.mCallback = listener;
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
        super.setBackgroundColor(color);
        mBaseColor = color;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsAnimRunning) {
            return false;
        }
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
            default:
                break;
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
        if (!mIsDragging || getCurrentView() == null || mIsAnimRunning) {
            return super.onTouchEvent(ev);
        }
        mVelocityTracker.addMovement(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCapturedOriginY = getCurrentView().getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = (ev.getRawY() - mDownY) / 5;
                getCurrentView().setY(mCapturedOriginY + deltaY);
                mFingerUpBackgroundAlpha = 1 - (Math.abs(deltaY) / mDragThresholdHeight);
                super.setBackgroundColor(ColorUtil.alphaColor(getBackgroundColor(), mFingerUpBackgroundAlpha));
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
                try {
                    mVelocityTracker.recycle();
                } catch (Throwable e) {
                    // ......
                }
                mIsDragging = false;
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        super.setAdapter(new PagerAdapterProxy(adapter));
    }

    @Nullable
    private View getCurrentView() {
        PagerAdapter adapter = getAdapter();
        if (adapter instanceof PagerAdapterProxy) {
            return ((PagerAdapterProxy) adapter).getCurrentView();
        }
        return null;
    }

    private void recover() {
        if (mIsAnimRunning || getCurrentView() == null) {
            return;
        }
        ValueAnimator animRecover = ValueAnimator.ofFloat(getCurrentView().getY(), mCapturedOriginY).setDuration(300);
        animRecover.setInterpolator(new OvershootInterpolator(3f));
        animRecover.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getCurrentView().setY((float) animation.getAnimatedValue());
                DraggableViewPager.super.setBackgroundColor(
                        ColorUtil.alphaColor(
                                getBackgroundColor(),
                                mFingerUpBackgroundAlpha + (1 - mFingerUpBackgroundAlpha) * animation.getAnimatedFraction()
                        )
                );
            }
        });
        animRecover.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimRunning = false;
            }
        });
        animRecover.start();
    }

    private void dismiss() {
        if (mCallback != null && mCallback.handleDismissAction()) {
            return;
        }
        if (mIsAnimRunning || getCurrentView() == null) {
            return;
        }
        float destY = (getCurrentView().getY() - mCapturedOriginY > 0 ? 1 : -1)
                * getResources().getDisplayMetrics().heightPixels;
        ValueAnimator animDismiss = ValueAnimator.ofFloat(getCurrentView().getY(), destY).setDuration(400);
        animDismiss.setInterpolator(new AnticipateInterpolator(1f));
        animDismiss.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getCurrentView().setY((float) animation.getAnimatedValue());
                DraggableViewPager.super.setBackgroundColor(
                        ColorUtil.alphaColor(
                                getBackgroundColor(),
                                mFingerUpBackgroundAlpha * (1 - animation.getAnimatedFraction())
                        )
                );
            }
        });
        animDismiss.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimRunning = false;
                if (mCallback != null) {
                    mCallback.onDismissed();
                }
            }

        });
        animDismiss.start();
    }

    private int getBackgroundColor() {
        return mBaseColor;
    }

    public interface Callback {

        void onPagerChanged(int position);

        /**
         * @return if true mean dismiss action has been consumed, false will consume by self.
         */
        boolean handleDismissAction();

        void onDismissed();

    }

    private static final class PagerAdapterProxy extends PagerAdapter {

        private View mCurrentView;
        private PagerAdapter mOriginAdapter;

        PagerAdapterProxy(PagerAdapter originAdapter) {
            mOriginAdapter = originAdapter;
        }

        public View getCurrentView() {
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
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            return mOriginAdapter.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            mOriginAdapter.destroyItem(container, position, object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return mOriginAdapter.getItemPosition(object);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (object instanceof View) {
                mCurrentView = (View) object;
            } else if (object instanceof Fragment) {
                mCurrentView = ((Fragment) object).getView();
            }
            mOriginAdapter.setPrimaryItem(container, position, object);
        }

        @Override
        public void setPrimaryItem(@NonNull View container, int position, @NonNull Object object) {
            if (object instanceof View) {
                mCurrentView = (View) object;
            } else if (object instanceof Fragment) {
                mCurrentView = ((Fragment) object).getView();
            }
            mOriginAdapter.setPrimaryItem(container, position, object);
        }

        @Override
        public void startUpdate(@NonNull ViewGroup container) {
            mOriginAdapter.startUpdate(container);
        }

        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            mOriginAdapter.finishUpdate(container);
        }

        @Override
        public void startUpdate(@NonNull View container) {
            mOriginAdapter.startUpdate(container);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull View container, int position) {
            return mOriginAdapter.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(@NonNull View container, int position, @NonNull Object object) {
            mOriginAdapter.destroyItem(container, position, object);
        }

        @Override
        public void finishUpdate(@NonNull View container) {
            mOriginAdapter.finishUpdate(container);
        }

        @Nullable
        @Override
        public Parcelable saveState() {
            return mOriginAdapter.saveState();
        }

        @Override
        public void restoreState(@Nullable Parcelable state, @Nullable ClassLoader loader) {
            mOriginAdapter.restoreState(state, loader);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mOriginAdapter.getPageTitle(position);
        }

        @Override
        public float getPageWidth(int position) {
            return mOriginAdapter.getPageWidth(position);
        }

        @Override
        public void notifyDataSetChanged() {
            mOriginAdapter.notifyDataSetChanged();
        }

        @Override
        public void registerDataSetObserver(@NonNull DataSetObserver observer) {
            mOriginAdapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(@NonNull DataSetObserver observer) {
            mOriginAdapter.unregisterDataSetObserver(observer);
        }

    }

}
