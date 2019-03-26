package com.sharry.picturepicker.camera;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.sharry.picturepicker.crop.CropCallback;
import com.sharry.picturepicker.crop.PictureCropManager;
import com.sharry.picturepicker.support.utils.FileUtil;
import com.sharry.picturepicker.support.utils.PictureUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 从相机拍照获取图片的 Fragment
 */
public class CameraRequestFragment extends Fragment {

    public static final String TAG = CameraRequestFragment.class.getSimpleName();
    public static final String INTENT_ACTION_START_CAMERA = "android.media.action.IMAGE_CAPTURE";

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE_TAKE = 0x000222;// 图片选择请求码

    public static CameraRequestFragment newInstance() {
        CameraRequestFragment fragment = new CameraRequestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private Context mContext;
    private CameraConfig mConfig;
    private CameraCallback mCameraCallback;
    private File mTempFile;                  // Temp file associated with camera.

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
    public void takePicture(CameraConfig config, CameraCallback callback) {
        this.mConfig = config;
        this.mCameraCallback = callback;
        mTempFile = FileUtil.createTempFileByDestDirectory(config.getCameraDirectoryPath());
        Uri tempUri = FileUtil.getUriFromFile(mContext, mConfig.getAuthority(), mTempFile);
        // 启动相机
        Intent intent = new Intent(INTENT_ACTION_START_CAMERA);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || null == mCameraCallback) return;
        switch (requestCode) {
            case REQUEST_CODE_TAKE:
                try {
                    // 1. 将拍摄后的图片, 压缩到 cameraDestFile 中
                    File cameraDestFile = FileUtil.createCameraDestFile(mConfig.getCameraDirectoryPath());
                    PictureUtil.doCompress(mTempFile.getAbsolutePath(), cameraDestFile.getAbsolutePath(),
                            mConfig.getCameraDestQuality());
                    // 2. 处理图片裁剪
                    if (mConfig.isCropSupport()) {
                        performCropPicture(cameraDestFile.getAbsolutePath());
                    } else {
                        // 3. 回调
                        mCameraCallback.onCameraTakeComplete(cameraDestFile.getAbsolutePath());
                        // 刷新文件管理器
                        FileUtil.freshMediaStore(mContext, cameraDestFile);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Picture compress failed after camera take.", e);
                } finally {
                    mTempFile.delete();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 处理裁剪
     */
    private void performCropPicture(String cameraFilePath) {
        PictureCropManager.with(mContext)
                .setConfig(
                        mConfig.getCropConfig().rebuild()
                                .setOriginFile(cameraFilePath)// 需要裁剪的文件路径
                                .build()
                )
                .crop(new CropCallback() {
                    @Override
                    public void onCropComplete(String path) {
                        mCameraCallback.onCameraTakeComplete(path);
                    }
                });
    }

}
