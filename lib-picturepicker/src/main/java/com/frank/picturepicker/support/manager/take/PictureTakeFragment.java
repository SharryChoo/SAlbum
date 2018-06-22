package com.frank.picturepicker.support.manager.take;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

import com.frank.picturepicker.support.callback.CropCallback;
import com.frank.picturepicker.support.callback.TakeCallback;
import com.frank.picturepicker.support.config.TakeConfig;
import com.frank.picturepicker.support.manager.crop.PictureCropManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 从相机拍照获取图片的 Fragment
 */
public class PictureTakeFragment extends Fragment {

    public static final String TAG = PictureTakeFragment.class.getSimpleName();
    public static final String INTENT_ACTION_START_CAMERA = "android.media.action.IMAGE_CAPTURE";
    public static final String RESULT_EXTRA_TAKE_PHOTO = "result_extra_take_photo";// 返回的图片

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE_TAKE = 0x00000111;// 图片选择请求码
    public static final int REQUEST_CODE_SETTING = 0x00000222;// 设置页面请求码
    public static final int REQUEST_CODE_PERMISSIONS = 0x0000333;// 权限申请请求码


    public static PictureTakeFragment newInstance() {
        PictureTakeFragment fragment = new PictureTakeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    // 回调
    private TakeCallback mTakeCallback;

    private Context mContext;
    // 存储系统相机拍摄的图片临时路径
    private File mTempCameraFile;
    // 相关配置
    private TakeConfig mConfig;

    /**
     * 权限请求的接口
     */
    public interface PermissionsCallback {
        void onResult(boolean granted);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * 开始拍照
     */
    public void takePicture(TakeConfig config, TakeCallback callback) {
        this.mConfig = config;
        this.mTakeCallback = callback;
        mTempCameraFile = createTempFile();
        // 启动相机
        Intent intent = new Intent(INTENT_ACTION_START_CAMERA);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, TakeUtil.getUriFromFile(mContext, mConfig.authority, mTempCameraFile));
        startActivityForResult(intent, REQUEST_CODE_TAKE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_TAKE || resultCode != Activity.RESULT_OK) return;
        try {
            // 1. 将拍摄后的图片, 压缩到 cameraDestFile 中
            TakeUtil.doCompress(mTempCameraFile.getAbsolutePath(), mConfig.cameraDestFilePath, mConfig.destQuality);
            // 2. 处理图片裁剪
            if (mConfig.isCropSupport) {
                performCropPicture();
            } else {
                // 3. 回调
                callCameraCallback(mConfig.cameraDestFilePath);
                // 刷新文件管理器
                TakeUtil.freshMediaStore(mContext, new File(mConfig.cameraDestFilePath));
            }
        } catch (IOException e) {
            Log.e(TAG, "Camera photo compress failed.", e);
        }
    }

    /**
     * 处理裁剪
     */
    private void performCropPicture() {
        PictureCropManager.with(mContext)
                .setFileProviderAuthority(mConfig.authority)
                .setOriginFilePath(mConfig.cameraDestFilePath)// 需要裁剪的文件路径
                .setDestFilePath(mConfig.cropDestFilePath)// 裁剪后输出的文件路径
                .setQuality(100)// 拍摄后已经压缩一次了, 裁剪时不压缩
                .crop(new CropCallback() {
                    @Override
                    public void onCropComplete(String path) {
                        callCameraCallback(path);
                    }
                });
    }

    /**
     * 回调相机的 Callback
     */
    private void callCameraCallback(String path) {
        mTakeCallback.onTakeComplete(path);
        mTempCameraFile.delete();
    }

    /**
     * 创建临时文件
     */
    private File createTempFile() {
        // 创建临时文件
        String tempFileName = "temp_" + SystemClock.currentThreadTimeMillis() + ".jpg";
        File tempFile = new File(mContext.getCacheDir(), tempFileName);
        try {
            if (tempFile.exists()) tempFile.delete();
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

}
