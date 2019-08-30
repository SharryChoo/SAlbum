package com.sharry.lib.picturepicker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * 从相机拍照获取图片的 Fragment
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 4:52 PM
 */
public class CameraRequestFragment extends Fragment {

    public static final String TAG = CameraRequestFragment.class.getSimpleName();
    private static final int REQUEST_CODE_TAKE = 454;
    public static final String INTENT_ACTION_START_CAMERA = "android.media.action.IMAGE_CAPTURE";

    /**
     * Get callback fragment from here.
     */
    @Nullable
    public static CameraRequestFragment getInstance(@NonNull Activity bind) {
        if (ActivityStateUtil.isIllegalState(bind)) {
            return null;
        }
        CameraRequestFragment callbackFragment = findFragmentFromActivity(bind);
        if (callbackFragment == null) {
            callbackFragment = new CameraRequestFragment();
            FragmentManager fragmentManager = bind.getFragmentManager();
            fragmentManager.beginTransaction()
                    .add(callbackFragment, TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return callbackFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private static CameraRequestFragment findFragmentFromActivity(@NonNull Activity activity) {
        return (CameraRequestFragment) activity.getFragmentManager().findFragmentByTag(TAG);
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
        if (resultCode != Activity.RESULT_OK || null == mCameraCallback) {
            return;
        }
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
        CropperManager.with(mContext)
                .setConfig(
                        mConfig.getCropConfig().rebuild()
                                .setOriginFile(cameraFilePath)// 需要裁剪的文件路径
                                .build()
                )
                .crop(new CropperCallback() {
                    @Override
                    public void onCropComplete(@NonNull String path) {
                        mCameraCallback.onCameraTakeComplete(path);
                    }
                });
    }

}
