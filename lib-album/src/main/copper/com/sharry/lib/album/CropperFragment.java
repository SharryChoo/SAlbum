package com.sharry.lib.album;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;

/**
 * 调用系统裁剪工具
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 4:53 PM
 */
public class CropperFragment extends Fragment {

    public static final String TAG = CropperFragment.class.getSimpleName();
    private static final int REQUEST_CODE_CROP = 446;
    public static final String INTENT_ACTION_START_CROP = "com.android.camera.action.CROP";

    /**
     * Get callback fragment from here.
     */
    @Nullable
    public static CropperFragment getInstance(@NonNull Activity bind) {
        if (ActivityStateUtil.isIllegalState(bind)) {
            return null;
        }
        CropperFragment callbackFragment = findFragmentFromActivity(bind);
        if (callbackFragment == null) {
            callbackFragment = new CropperFragment();
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
    private static CropperFragment findFragmentFromActivity(@NonNull Activity activity) {
        return (CropperFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

    private File mTempFile;
    private Context mContext;
    private CropperConfig mConfig;
    private CropperCallback mCropperCallback;

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
     * 开始裁剪
     */
    public void cropPicture(CropperConfig config, CropperCallback callback) {
        this.mConfig = config;
        this.mCropperCallback = callback;
        // Create temp file associated with crop function.
        mTempFile = FileUtil.createTempJpegFile(mContext);
        try {
            // Get URI associated with target file.
            Uri tempUri = FileUtil.getUriFromFile(mContext, config.getAuthority(), mTempFile);
            // Completion intent instance.
            Intent intent = new Intent(INTENT_ACTION_START_CROP);
            completion(intent, config, config.getOriginUri(), tempUri);
            // launch crop Activity
            startActivityForResult(intent, REQUEST_CODE_CROP);
        } catch (Throwable e) {
            // ignore.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || null == mCropperCallback) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_CROP:
                try {
                    // 创建最终的目标文件, 将图片从临时文件压缩到指定的目录
                    if (VersionUtil.isQ()) {
                        Uri uri = FileUtil.createJpegPendingItem(mContext, mConfig.getRelativePath());
                        ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(uri, "w");
                        CompressUtil.doCompress(mTempFile.getAbsolutePath(), pfd.getFileDescriptor(), mConfig.getDestQuality());
                        FileUtil.publishPendingItem(mContext, uri);
                        MediaMeta mediaMeta = MediaMeta.create(uri, FileUtil.getImagePath(mContext, uri), true);
                        mCropperCallback.onCropComplete(mediaMeta);
                    } else {
                        File file = FileUtil.createJpegFile(mContext, mConfig.getRelativePath());
                        Uri uri = FileUtil.getUriFromFile(mContext, mConfig.getAuthority(), file);
                        ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(uri, "w");
                        CompressUtil.doCompress(mTempFile.getAbsolutePath(), pfd.getFileDescriptor(), mConfig.getDestQuality());
                        FileUtil.notifyMediaStore(mContext, file.getAbsolutePath());
                        MediaMeta mediaMeta = MediaMeta.create(uri, file.getAbsolutePath(), true);
                        mCropperCallback.onCropComplete(mediaMeta);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Picture compress failed after crop.", e);
                } finally {
                    if (mTempFile != null) {
                        mTempFile.delete();
                    }
                }
                break;
            default:
                break;
        }

    }

    private void completion(Intent intent, CropperConfig config, Uri originUri, Uri tempUri) {
        // 可以选择图片类型, 如果是*表明所有类型的图片
        intent.setDataAndType(originUri, "image/*");
        // 设置可裁剪状态
        intent.putExtra("crop", true);
        // 裁剪时是否保留图片的比例, 这里的比例是1:1
        intent.putExtra("scale", config.getAspectX() == config.getAspectY());
        // X方向上的比例
        intent.putExtra("aspectX", config.getAspectX());
        // Y方向上的比例
        intent.putExtra("aspectY", config.getAspectY());
        // 裁剪区域的宽
        intent.putExtra("outputX", config.getOutputX());
        // 裁剪区域的宽
        intent.putExtra("outputY", config.getOutputY());
        // 是否将数据保留在Bitmap中返回, 返回的缩略图效果模糊
        intent.putExtra("return-data", false);
        // 设置输出的格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // 裁剪后的保存路径, 这里的 URI 不需要区分
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        // 不启用人脸识别
        intent.putExtra("noFaceDetection", true);
        // 安卓 6.0 以上版本添加读写权限请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // 将存储图片的 uri 读写权限授权给剪裁工具应用
            List<ResolveInfo> resInfoList = mContext.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                int modeFlags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getActivity().grantUriPermission(packageName, originUri, modeFlags);
                getActivity().grantUriPermission(packageName, tempUri, modeFlags);
            }
        }
    }

}
