package com.sharry.picturepicker.support.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.Property;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.sharry.picturepicker.watcher.impl.SharedElementData;
import com.sharry.picturepicker.widget.photoview.PhotoView;

/**
 * Picture watcher shared elements jump helper.
 * <p>
 * Thanks for google framework sources, {@link android.transition.ChangeBounds}
 * and {@link android.transition.ChangeImageTransform}
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 3/18/2019 11:13 AM
 */
public class SharedElementUtils {

    private static Property<PhotoView, Matrix> ANIMATED_TRANSFORM_PROPERTY
            = new Property<PhotoView, Matrix>(Matrix.class, "animatedTransform") {
        @Override
        public void set(PhotoView photoView, Matrix matrix) {
            photoView.animateTransform(matrix);
        }

        @Override
        public Matrix get(PhotoView object) {
            return null;
        }
    };

    private static final Property<ViewBounds, PointF> TOP_LEFT_PROPERTY =
            new Property<ViewBounds, PointF>(PointF.class, "topLeft") {
                @Override
                public void set(ViewBounds viewBounds, PointF topLeft) {
                    viewBounds.setTopLeft(topLeft);
                }

                @Override
                public PointF get(ViewBounds viewBounds) {
                    return null;
                }
            };

    private static final Property<ViewBounds, PointF> BOTTOM_RIGHT_PROPERTY =
            new Property<ViewBounds, PointF>(PointF.class, "bottomRight") {
                @Override
                public void set(ViewBounds viewBounds, PointF bottomRight) {
                    viewBounds.setBottomRight(bottomRight);
                }

                @Override
                public PointF get(ViewBounds viewBounds) {
                    return null;
                }
            };

    /**
     * Create enter animator for PictureWatcher.
     *
     * @param target exchange target.
     * @param data   origin data.
     */
    public static Animator createSharedElementEnterAnimator(PhotoView target, SharedElementData data) {
        int[] locations = new int[2];
        target.getLocationOnScreen(locations);
        target.setPivotX(0);
        target.setPivotY(0);
        AnimatorSet enterAnimators = new AnimatorSet();
        if (target.getDrawable() == null) {
            return enterAnimators;
        }
        enterAnimators.playTogether(
                ObjectAnimator.ofFloat(target, "scaleX", data.width / (float) target.getWidth(), 1f),
                ObjectAnimator.ofFloat(target, "scaleY", data.height / (float) target.getHeight(), 1f),
                ObjectAnimator.ofFloat(target, "translationX", data.startX - locations[0], 0),
                ObjectAnimator.ofFloat(target, "translationY", data.startY - locations[1], 0)
        );
        enterAnimators.setInterpolator(new OvershootInterpolator(2f));
        enterAnimators.setDuration(500);
        return enterAnimators;
    }

    /**
     * Create exit animator for PictureWatcher.
     *
     * @param target exchange target.
     * @param data   origin data.
     */
    public static Animator createSharedElementExitAnimator(PhotoView target, SharedElementData data) {
        AnimatorSet exitAnimators = new AnimatorSet();
        if (target.getDrawable() == null) {
            return exitAnimators;
        }
        // 设置尺寸动画 ChangeBounds
        AnimatorSet boundsAnim = getBoundsChangedAnim(target, data);
        // 设置缩放动画
        final ObjectAnimator matrixAnim = ObjectAnimator.ofObject(target, ANIMATED_TRANSFORM_PROPERTY,
                new MatrixEvaluator(),
                target.getImageMatrix(),
                centerCropMatrix(
                        target.getDrawable().getIntrinsicWidth(),
                        target.getDrawable().getIntrinsicHeight(),
                        data.width,
                        data.height
                )
        );
        exitAnimators.playTogether(boundsAnim, matrixAnim);
        exitAnimators.setDuration(400);
        exitAnimators.setInterpolator(new DecelerateInterpolator());
        return exitAnimators;
    }

    /**
     * Create bounds changed animator for exit animator.
     *
     * @param target exchange target.
     * @param data   origin data.
     */
    private static AnimatorSet getBoundsChangedAnim(PhotoView target, SharedElementData data) {
        final ViewBounds viewBounds = new ViewBounds(target);
        int[] locations = new int[2];
        target.getLocationOnScreen(locations);
        target.setPivotX(0);
        // 构建左上的位移动画
        Path topLeftPath = new Path();
        topLeftPath.moveTo(target.getLeft(), target.getTop());
        topLeftPath.lineTo(data.startX - locations[0], data.startY - locations[1]);
        ObjectAnimator topLeftAnimator = ObjectAnimator.ofFloat(viewBounds,
                new PathProperty<>(TOP_LEFT_PROPERTY, topLeftPath), 0f, 1f);
        // 构建右下的位移动画
        Path bottomRightPath = new Path();
        bottomRightPath.moveTo(target.getBottom(), target.getHeight());
        bottomRightPath.lineTo(data.startX + data.width - locations[0],
                data.startY + data.height - locations[1]);
        ObjectAnimator bottomRightAnimator = ObjectAnimator.ofFloat(viewBounds,
                new PathProperty<>(BOTTOM_RIGHT_PROPERTY, bottomRightPath), 0f, 1f);
        // 构建动画集合
        AnimatorSet set = new AnimatorSet();
        set.playTogether(topLeftAnimator, bottomRightAnimator);
        return set;
    }

    private static class ViewBounds {

        private int mLeft;
        private int mTop;
        private int mRight;
        private int mBottom;
        private View mView;
        private int mTopLeftCalls;
        private int mBottomRightCalls;

        ViewBounds(View view) {
            mView = view;
        }

        void setTopLeft(PointF topLeft) {
            mLeft = Math.round(topLeft.x);
            mTop = Math.round(topLeft.y);
            mTopLeftCalls++;
            if (mTopLeftCalls == mBottomRightCalls) {
                setLeftTopRightBottom();
            }
        }

        void setBottomRight(PointF bottomRight) {
            mRight = Math.round(bottomRight.x);
            mBottom = Math.round(bottomRight.y);
            mBottomRightCalls++;
            if (mTopLeftCalls == mBottomRightCalls) {
                setLeftTopRightBottom();
            }
        }

        private void setLeftTopRightBottom() {
            mView.setLeft(mLeft);
            mView.setTop(mTop);
            mView.setRight(mRight);
            mView.setBottom(mBottom);
            mTopLeftCalls = 0;
            mBottomRightCalls = 0;
        }

    }

    private static class PathProperty<T> extends Property<T, Float> {

        private final Property<T, PointF> mProperty;
        private final PathMeasure mPathMeasure;
        private final float mPathLength;
        private final float[] mPosition = new float[2];
        private final PointF mPointF = new PointF();
        private float mCurrentFraction;

        PathProperty(Property<T, PointF> property, Path path) {
            super(Float.class, property.getName());
            mProperty = property;
            mPathMeasure = new PathMeasure(path, false);
            mPathLength = mPathMeasure.getLength();
        }

        @Override
        public Float get(T object) {
            return mCurrentFraction;
        }

        @Override
        public void set(T target, Float fraction) {
            mCurrentFraction = fraction;
            mPathMeasure.getPosTan(mPathLength * fraction, mPosition, null);
            mPointF.x = mPosition[0];
            mPointF.y = mPosition[1];
            mProperty.set(target, mPointF);
        }

    }

    /**
     * Calculates the image transformation matrix for an ImageView with ScaleType CENTER_CROP. This
     * needs to be manually calculated for consistent behavior across all the API levels.
     */
    private static Matrix centerCropMatrix(int startWidth, int startHeight, int destWidth, int destHeight) {
        final float scaleX = ((float) destWidth) / startWidth;
        final float scaleY = ((float) destHeight) / startHeight;

        final float maxScale = Math.max(scaleX, scaleY);
        final float width = startWidth * maxScale;
        final float height = startHeight * maxScale;
        final int tx = Math.round((destWidth - width) / 2f);
        final int ty = Math.round((destHeight - height) / 2f);

        final Matrix matrix = new Matrix();
        matrix.postScale(maxScale, maxScale);
        matrix.postTranslate(tx, ty);
        return matrix;
    }

    /**
     * The evaluator associated with matrix animator.
     */
    public static class MatrixEvaluator implements TypeEvaluator<Matrix> {

        float[] mTempStartValues = new float[9];

        float[] mTempEndValues = new float[9];

        Matrix mTempMatrix = new Matrix();

        @Override
        public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
            startValue.getValues(mTempStartValues);
            endValue.getValues(mTempEndValues);
            for (int i = 0; i < 9; i++) {
                float diff = mTempEndValues[i] - mTempStartValues[i];
                mTempEndValues[i] = mTempStartValues[i] + (fraction * diff);
            }
            mTempMatrix.setValues(mTempEndValues);
            return mTempMatrix;
        }
    }

}
