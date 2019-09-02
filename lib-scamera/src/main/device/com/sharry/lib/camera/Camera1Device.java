package com.sharry.lib.camera;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import androidx.collection.SparseArrayCompat;

import java.util.List;
import java.util.SortedSet;

/**
 * Camera1 实现的相机引擎
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-04-24
 */
class Camera1Device extends AbsCameraDevice {

    private static final String TAG = Camera1Device.class.getSimpleName();
    private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

    static {
        FLASH_MODES.put(Constants.FLASH_OFF, Camera.Parameters.FLASH_MODE_OFF);
        FLASH_MODES.put(Constants.FLASH_ON, Camera.Parameters.FLASH_MODE_ON);
        FLASH_MODES.put(Constants.FLASH_TORCH, Camera.Parameters.FLASH_MODE_TORCH);
        FLASH_MODES.put(Constants.FLASH_AUTO, Camera.Parameters.FLASH_MODE_AUTO);
        FLASH_MODES.put(Constants.FLASH_RED_EYE, Camera.Parameters.FLASH_MODE_RED_EYE);
    }

    private static final int MAGIC_TEXTURE_ID = 0;
    private static final int INVALID_CAMERA_ID = -1;

    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private final SizeMap mPreviewSizes = new SizeMap();
    private final SizeMap mPictureSizes = new SizeMap();
    private final SurfaceTexture mBufferTexture;

    private Camera mImpl;
    private Camera.Parameters mCameraParams;

    Camera1Device(CameraContext context, OnCameraReadyListener listener) {
        super(context, listener);
        mBufferTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
    }

    @Override
    public boolean isCameraOpened() {
        return mImpl != null;
    }

    @Override
    public void open() {
        // Stop preview first.
        close();
        // 根据 Options 初始化相机
        startPreviewInternal();
    }

    @Override
    public void close() {
        if (null != mImpl) {
            try {
                // 停止预览
                mImpl.stopPreview();
                /*
                 移除回调, 否则会扔出: Camera is being used after Camera.release() was called
                */
                mImpl.setPreviewCallback(null);
                mImpl.release();
                mImpl = null;
                Log.i(TAG, "Camera release success.");
            } catch (Throwable e) {
                // ignore.
            }
        }
    }

    @Override
    public Bitmap takePicture() {
        close();
        return null;
    }

    @Override
    public void notifyAutoFocusChanged() {
        // is previewing
        if (isCameraOpened()) {
            if (this.autoFocus == context.isAutoFocus()) {
                return;
            }
            this.autoFocus = context.isAutoFocus();
            // resetMatrix params
            setAutoFocusInternal(autoFocus);
            mImpl.setParameters(mCameraParams);
        }
        // not previewing
        else {
            this.autoFocus = context.isAutoFocus();
        }
    }

    @Override
    public void notifyFlashModeChanged() {
        // is previewing
        if (isCameraOpened()) {
            if (flashMode == context.getFlashMode()) {
                return;
            }
            // resetMatrix params
            if (setFlashInternal(context.getFlashMode())) {
                mImpl.setParameters(mCameraParams);
            }
        }
        // not previewing
        else {
            flashMode = context.getFlashMode();
        }
    }

    /**
     * 开启预览真正的逻辑实现
     */
    private void startPreviewInternal() {
        try {
            // 1. 打开相机
            int cameraId = chooseCamera(facing);
            mImpl = Camera.open(cameraId);
            // 2. 设置相机参数
            mCameraParams = mImpl.getParameters();
            /*
             3. 设置预览尺寸
             */
            // 采集所有的预览尺寸
            mPreviewSizes.clear();
            for (Camera.Size size : mCameraParams.getSupportedPreviewSizes()) {
                mPreviewSizes.add(new Size(size.width, size.height));
            }
            // 获取用户期望的比例的集合
            SortedSet<Size> previewSizes = mPreviewSizes.sizes(aspectRatio);
            if (previewSizes == null) {
                // 用户期望的比例不存在, 获取默认比例
                previewSizes = mPreviewSizes.sizes(chooseDefaultAspectRatio());
            }
            final Size previewSize = chooseOptimalPreviewSize(previewSizes);
            mCameraParams.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());

            /*
             4. 设置拍照尺寸
             */
            // 采集所有照片的尺寸
            mPictureSizes.clear();
            for (Camera.Size size : mCameraParams.getSupportedPictureSizes()) {
                mPictureSizes.add(new Size(size.width, size.height));
            }
            // 获取用户期望的比例集合
            SortedSet<Size> pictureSizes = mPictureSizes.sizes(aspectRatio);
            if (pictureSizes == null) {
                // 用户期望的尺寸不存在, 获取默认比例
                pictureSizes = mPreviewSizes.sizes(chooseDefaultAspectRatio());
            }
            // 选择期望集合中, 尺寸最大的一个, 保证拍照后输出图像的清晰度
            Size pictureSize = pictureSizes.last();
            mCameraParams.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
            // 5. 设置拍摄后的图像输出的方向
            mCameraParams.setRotation(calcTakenPictureRotation(screenOrientationDegrees));
            // 6. 处理自动对焦
            setAutoFocusInternal(autoFocus);
            // 7. 处理闪光灯变化
            setFlashInternal(flashMode);
            mImpl.setParameters(mCameraParams);
            // 8. 设置预览帧的图像的输出方向
            mImpl.setDisplayOrientation(calcPreviewFrameOrientation(screenOrientationDegrees));
            // 9. 设置图像输出的画布
            mImpl.setPreviewTexture(mBufferTexture);
            // 10. 启动预览
            mImpl.startPreview();
            // 6. 通知外界, Camera 数据准备好了
            listener.onCameraReady(mBufferTexture, previewSize, 0);
            Log.i(TAG, "Camera start preview success.");
        } catch (Throwable e) {
            Log.e(TAG, "Camera start preview failed.", e);
            close();
        }
    }

    /**
     * 选择相机 id
     */
    private int chooseCamera(int facing) {
        int cameraId = INVALID_CAMERA_ID;
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            if (mCameraInfo.facing == facing) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    /**
     * 获取默认比例
     */
    private AspectRatio chooseDefaultAspectRatio() {
        AspectRatio result = null;
        for (AspectRatio ratio : mPreviewSizes.ratios()) {
            result = ratio;
            if (AspectRatio.DEFAULT.equals(ratio)) {
                break;
            }
        }
        return result;
    }

    /**
     * 设置自动对焦
     * <p>
     * it will modify {@link #mCameraParams}.
     */
    private void setAutoFocusInternal(boolean autoFocus) {
        final List<String> modes = mCameraParams.getSupportedFocusModes();
        if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            mCameraParams.setFocusMode(modes.get(0));
        }
    }

    /**
     * 设置闪光灯
     *
     * @return {@code true} if {@link #mCameraParams} was modified.
     */
    private boolean setFlashInternal(int flash) {
        List<String> modes = mCameraParams.getSupportedFlashModes();
        String mode = FLASH_MODES.get(flash);
        if (modes != null && modes.contains(mode)) {
            mCameraParams.setFlashMode(mode);
            flashMode = flash;
            return true;
        }
        String currentMode = FLASH_MODES.get(flashMode);
        if (modes == null || !modes.contains(currentMode)) {
            mCameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            flashMode = Constants.FLASH_OFF;
            return true;
        }
        return false;
    }

    /**
     * 选择最合适的预览尺寸
     */
    private Size chooseOptimalPreviewSize(SortedSet<Size> sizes) {
        int desiredWidth;
        int desiredHeight;
        if (isLandscape(screenOrientationDegrees)) {
            desiredWidth = previewWidth;
            desiredHeight = previewHeight;
        } else {
            desiredWidth = previewHeight;
            desiredHeight = previewWidth;
        }
        Size result = null;
        for (Size size : sizes) {
            result = size;
            // Iterate from small to large
            if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                break;
            }
        }
        return result;
    }

    /**
     * Calculate camera rotate
     * <p>
     * This calculation is applied to the output JPEG either via Exif Orientation tag
     * or by actually transforming the bitmap. (Determined by vendor camera API implementation)
     * <p>
     * Note: This is not the same calculation as the display orientation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees to rotate image in order for it to view correctly.
     */
    private int calcTakenPictureRotation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (mCameraInfo.orientation + screenOrientationDegrees) % 360;
        } else {  // back-facing
            final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
            return (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360;
        }
    }

    /**
     * Calculate display orientation
     * https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
     * <p>
     * This calculation is used for orienting the preview
     * <p>
     * Note: This is not the same calculation as the camera rotate
     *
     * @param screenOrientationDegrees Screen orientation in degrees(anticlockwise)
     * @return Number of degrees required to rotate preview
     */
    private int calcPreviewFrameOrientation(int screenOrientationDegrees) {
        int result;
        // front-facing
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCameraInfo.orientation + screenOrientationDegrees) % 360;
            // compensate the mirror
            result = (360 - result) % 360;
        }
        // back-facing
        else {
            result = (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360;
        }
        return result;
    }

    /**
     * Test if the supplied orientation is in landscape.
     *
     * @param screenOrientationDegrees Orientation in degrees (0,90,180,270)
     * @return True if in landscape, false if portrait
     */
    private boolean isLandscape(int screenOrientationDegrees) {
        return (screenOrientationDegrees == Constants.LANDSCAPE_90
                || screenOrientationDegrees == Constants.LANDSCAPE_270);
    }

}
