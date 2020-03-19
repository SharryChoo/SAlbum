package com.sharry.lib.camera;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The facade handle device interaction with view.
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-08-05
 */
public class SCameraView extends FrameLayout implements
        ScreenOrientationDetector.OnDisplayChangedListener, ICameraDevice.OnCameraReadyListener {

    /**
     * The camera device faces the opposite direction as the device's screen.
     */
    public static final int FACING_BACK = Constants.FACING_BACK;

    /**
     * The camera device faces the same direction as the device's screen.
     */
    public static final int FACING_FRONT = Constants.FACING_FRONT;

    /**
     * Direction the camera faces relative to device screen.
     */
    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    @interface Facing {
    }

    /**
     * Flash will not be fired.
     */
    public static final int FLASH_OFF = Constants.FLASH_OFF;

    /**
     * Flash will always be fired during snapshot.
     */
    public static final int FLASH_ON = Constants.FLASH_ON;

    /**
     * Constant emission of light during preview, auto-focus and snapshot.
     */
    public static final int FLASH_TORCH = Constants.FLASH_TORCH;

    /**
     * Flash will be fired automatically when required.
     */
    public static final int FLASH_AUTO = Constants.FLASH_AUTO;

    /**
     * Flash will be fired in red-eye reduction mode.
     */
    public static final int FLASH_RED_EYE = Constants.FLASH_RED_EYE;

    /**
     * The mode for for the camera device's flash control
     */
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO, FLASH_RED_EYE})
    @interface Flash {
    }

    /**
     * Control camera device
     */
    private final ICameraDevice mDevice;

    /**
     * The context holder data
     */
    private final CameraContext mContext;

    /**
     * Control preview.
     */
    private final IPreviewer mPreviewer;

    /**
     * Control display rotate
     */
    private final ScreenOrientationDetector mScreenOrientationDetector;

    public SCameraView(@NonNull Context context) {
        this(context, null);
    }

    public SCameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = new CameraContext(context);
        this.mPreviewer = new Previewer(context, this);
        this.mScreenOrientationDetector = new ScreenOrientationDetector(context, this);
        this.mDevice = new Camera1Device(mContext, this);
        // Attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SCameraView, defStyleAttr,
                R.style.Widget_CameraView);
        // set adjust view bounds
        setAdjustViewBounds(a.getBoolean(R.styleable.SCameraView_android_adjustViewBounds, false));
        // set facing
        setFacing(a.getInt(R.styleable.SCameraView_facing, FACING_BACK));
        // set aspect ratio
        String aspectRatio = a.getString(R.styleable.SCameraView_aspectRatio);
        setAspectRatio(aspectRatio != null ? AspectRatio.parse(aspectRatio) : AspectRatio.DEFAULT);
        // set auto focus
        setAutoFocus(a.getBoolean(R.styleable.SCameraView_autoFocus, true));
        // set flash mode
        setFlash(a.getInt(R.styleable.SCameraView_flash, Constants.FLASH_AUTO));
        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mScreenOrientationDetector.enable(getDisplay());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            mScreenOrientationDetector.disable();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onDisplayOrientationChanged(int displayOrientation) {
        mContext.setScreenOrientationDegrees(displayOrientation);
        mDevice.notifyScreenOrientationChanged();
    }

    @Override
    public void onCameraReady(@NonNull SurfaceTexture dataSource, @NonNull Size size, int rotation) {
        mPreviewer.setDataSource(dataSource);
        mPreviewer.setRotate(rotation);
        mPreviewer.setScaleType(ScaleType.CENTER_CROP, mScreenOrientationDetector.isLandscape(), size);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isInEditMode()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (!mContext.isAdjustViewBounds()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Handle android:adjustViewBounds
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 宽为精确测量
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            // 根据比例计算高
            final AspectRatio ratio = getAspectRatio();
            int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * ratio.toFloat());
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
            }
            super.onMeasure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
        // 高为精确测量
        else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            // 根据比例计算宽
            final AspectRatio ratio = getAspectRatio();
            int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * ratio.toFloat());
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        // Measure the PreviewView
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        // 若为横屏, 则颠倒一下比例, 方便计算
        AspectRatio ratio = getAspectRatio();
        if (!mScreenOrientationDetector.isLandscape()) {
            ratio = ratio.inverse();
        }
        if (height < width * ratio.getY() / ratio.getX()) {
            mPreviewer.getView().measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(width * ratio.getY() / ratio.getX(),
                            MeasureSpec.EXACTLY));
        } else {
            mPreviewer.getView().measure(
                    MeasureSpec.makeMeasureSpec(height * ratio.getX() / ratio.getY(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mContext.setDesiredSize(mPreviewer.getSize());
        mDevice.notifyDesiredSizeChanged();
    }

    /**
     * Open a camera device and start showing camera preview. This is typically called from
     * {@link Activity#onResume}.
     */
    public void startPreview() {
        post(new Runnable() {
            @Override
            public void run() {
                mDevice.open();
            }
        });
    }

    /**
     * Stop camera preview and close the device. This is typically called from
     * {@link Activity#onPause}
     */
    public void stopPreview() {
        mDevice.close();
    }

    /**
     * 获取照片
     */
    @Nullable
    public Bitmap takePicture() {
        stopPreview();
        return mPreviewer.getBitmap();
    }

    /**
     * Chooses camera by the direction it faces.
     *
     * @param facing The camera facing. Must be either {@link #FACING_BACK} or
     *               {@link #FACING_FRONT}.
     */
    public void setFacing(@Facing int facing) {
        mContext.setFacing(facing);
        mDevice.notifyFacingChanged();
    }

    /**
     * Sets the aspect ratio of camera.
     *
     * @param ratio The {@link AspectRatio} to be set.
     */
    public void setAspectRatio(@NonNull AspectRatio ratio) {
        if (mContext.getAspectRatio().equals(ratio)) {
            return;
        }
        mContext.setAspectRatio(ratio);
        mDevice.notifyAspectRatioChanged();
        requestLayout();
    }

    /**
     * Enables or disables the continuous auto-focus mode. When the current camera doesn't support
     * auto-focus, calling this method will be ignored.
     *
     * @param autoFocus {@code true} to enable continuous auto-focus mode. {@code false} to
     *                  disable it.
     */
    public void setAutoFocus(boolean autoFocus) {
        mContext.setAutoFocus(autoFocus);
        mDevice.notifyAutoFocusChanged();
    }

    /**
     * Sets the flash mode.
     *
     * @param flash The desired flash mode.
     */
    public void setFlash(@Flash int flash) {
        mContext.setFlashMode(flash);
        mDevice.notifyFlashModeChanged();
    }

    /**
     * @param adjustViewBounds {@code true} if you want the CameraView to adjust its bounds to
     *                         preserve the aspect ratio of camera.
     */
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (mContext.isAdjustViewBounds() != adjustViewBounds) {
            mContext.setAdjustViewBounds(adjustViewBounds);
            requestLayout();
        }
    }

    /**
     * Gets the direction that the current camera faces.
     *
     * @return The camera facing.
     */
    public int getFacing() {
        return mContext.getFacing();
    }

    /**
     * Gets the current aspect ratio of camera.
     *
     * @return The current {@link AspectRatio}. Default is 4:3.
     */
    @NonNull
    public AspectRatio getAspectRatio() {
        return mContext.getAspectRatio();
    }

    /**
     * Returns whether the continuous auto-focus mode is enabled.
     *
     * @return {@code true} if the continuous auto-focus mode is enabled. {@code false} if it is
     * disabled, or if it is not supported by the current camera.
     */
    public boolean getAutoFocus() {
        return mContext.isAutoFocus();
    }

    /**
     * Gets the current flash mode.
     *
     * @return The current flash mode.
     */
    public int getFlash() {
        //noinspection WrongConstant
        return mContext.getFlashMode();
    }

    /**
     * Returns whether the adjustViewBounds is enabled.
     *
     * @return {@code true} if the adjustViewBounds is enabled. {@code false} if it is disabled
     */
    public boolean getAdjustViewBounds() {
        return mContext.getAdjustViewBounds();
    }

    /**
     * Gets the previewer.
     *
     * @return The render view.
     */
    public IPreviewer getPreviewer() {
        return mPreviewer;
    }

    /**
     * Returns whether the display orientation is landscape.
     *
     * @return {@code true} if the adjustViewBounds is landscape.
     */
    public boolean isLandscape() {
        return mScreenOrientationDetector.isLandscape();
    }

}
