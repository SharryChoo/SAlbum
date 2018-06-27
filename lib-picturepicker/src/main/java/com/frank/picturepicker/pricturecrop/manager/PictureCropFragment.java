package com.frank.picturepicker.pricturecrop.manager;

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

import com.frank.picturepicker.support.util.Utils;

import java.io.File;
import java.util.List;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 调用系统裁剪工具
 */
public class PictureCropFragment extends Fragment {

    public static final String TAG = PictureCropFragment.class.getSimpleName();
    public static final String INTENT_ACTION_START_CROP = "com.android.camera.action.CROP";

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE_CROP = 0x00001222;// 图片选择请求码

    public static PictureCropFragment newInstance() {
        PictureCropFragment fragment = new PictureCropFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    // 回调
    private CropCallback mCropCallback;
    private Context mContext;
    // 相关配置
    private CropConfig mConfig;
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
        // 创建 TempFile
        mTempFile = Utils.createTempFileByDestFile(config.destFilePath);
        // 获取 URI
        Uri originUri = Utils.getUriFromFile(mContext, config.authority, new File(config.originFilePath));
        Uri tempUri = Utils.getUriFromFile(mContext, config.authority, mTempFile);
        // 启动相机
        Intent intent = new Intent(INTENT_ACTION_START_CROP);
        intent.setDataAndType(originUri, "image/*");//可以选择图片类型, 如果是*表明所有类型的图片
        intent.putExtra("crop", true);//设置可裁剪状态
        intent.putExtra("scale", config.aspectX == config.aspectY);//裁剪时是否保留图片的比例, 这里的比例是1:1
        intent.putExtra("aspectX", config.aspectX);// X方向上的比例
        intent.putExtra("aspectY", config.aspectY);// Y方向上的比例
        intent.putExtra("outputX", config.outputX);// 裁剪区域的宽
        intent.putExtra("outputY", config.outputY);// 裁剪区域的宽
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
        startActivityForResult(intent, REQUEST_CODE_CROP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_CROP ||
                resultCode != Activity.RESULT_OK || mCropCallback == null)
            return;
        try {
            // 将图片压缩到指定文夹(destFilePath)
            Utils.doCompress(mTempFile.getAbsolutePath(), mConfig.destFilePath, mConfig.destQuality);
            // 回调
            mCropCallback.onCropComplete(mConfig.destFilePath);
            // 通知文件变更
            Utils.freshMediaStore(mContext, new File(mConfig.destFilePath));
        } catch (Exception e) {
            Log.e(TAG, "Picture compress failed after crop.", e);
        } finally {
            mTempFile.delete(); // 删除临时文件
        }
    }

}
