package com.sharry.picturepicker.crop.manager;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.sharry.picturepicker.support.utils.FileUtil;
import com.sharry.picturepicker.support.utils.PictureUtil;

import java.io.File;
import java.util.List;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 调用系统裁剪工具
 */
public class PictureCropFragment extends Fragment {

    public static final String TAG = PictureCropFragment.class.getSimpleName();
    public static final String INTENT_ACTION_START_CROP = "com.android.camera.action.CROP";

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE_CROP = 0x000444;// 图片选择请求码

    public static PictureCropFragment newInstance() {
        PictureCropFragment fragment = new PictureCropFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private Context mContext;
    private CropConfig mConfig;
    private CropCallback mCropCallback;
    private File mTempFile;

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
    public void cropPicture(CropConfig config, CropCallback callback) {
        this.mConfig = config;
        this.mCropCallback = callback;
        // Create temp file associated with crop function.
        mTempFile = FileUtil.createTempFileByDestDirectory(config.getCropDirectoryPath());
        // Get URI associated with target file.
        Uri originUri = FileUtil.getUriFromFile(mContext, config.getAuthority(), new File(config.getOriginFilePath()));
        Uri tempUri = FileUtil.getUriFromFile(mContext, config.getAuthority(), mTempFile);
        // Completion intent instance.
        Intent intent = new Intent(INTENT_ACTION_START_CROP);
        completion(intent, config, originUri, tempUri);
        // launch crop Activity
        startActivityForResult(intent, REQUEST_CODE_CROP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || null == mCropCallback) return;
        switch (requestCode) {
            case REQUEST_CODE_CROP:
                try {
                    // 创建最终的目标文件, 将图片从临时文件压缩到指定的目录
                    File destFile = FileUtil.createCropDestFile(mConfig.getCropDirectoryPath());
                    PictureUtil.doCompress(mTempFile.getAbsolutePath(), destFile.getAbsolutePath(), mConfig.getDestQuality());
                    // 回调
                    mCropCallback.onCropComplete(destFile.getAbsolutePath());
                    // 通知文件变更
                    FileUtil.freshMediaStore(mContext, destFile);
                } catch (Exception e) {
                    Log.e(TAG, "Picture compress failed after crop.", e);
                } finally {
                    mTempFile.delete(); // 删除临时文件
                }
                break;
            default:
                break;
        }

    }

    private void completion(Intent intent, CropConfig config, Uri originUri, Uri tempUri) {
        intent.setDataAndType(originUri, "image/*");//可以选择图片类型, 如果是*表明所有类型的图片
        intent.putExtra("crop", true);//设置可裁剪状态
        intent.putExtra("scale", config.getAspectX() == config.getAspectY());//裁剪时是否保留图片的比例, 这里的比例是1:1
        intent.putExtra("aspectX", config.getAspectX());// X方向上的比例
        intent.putExtra("aspectY", config.getAspectY());// Y方向上的比例
        intent.putExtra("outputX", config.getOutputX());// 裁剪区域的宽
        intent.putExtra("outputY", config.getOutputY());// 裁剪区域的宽
        intent.putExtra("return-data", false);// 是否将数据保留在Bitmap中返回, 返回的缩略图效果模糊
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//设置输出的格式
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);// 裁剪后的保存路径, 这里的 URI 不需要区分
        intent.putExtra("noFaceDetection", true);// 不启用人脸识别
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
