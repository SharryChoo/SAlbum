package com.frank.picturepicker.support.manager.crop;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import com.frank.picturepicker.support.callback.CropCallback;
import com.frank.picturepicker.support.config.CropConfig;

import java.io.File;
import java.io.IOException;

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
    public void cropPicture(CropConfig config, CropCallback callback) {
        this.mConfig = config;
        this.mCropCallback = callback;
        // 启动相机
        Intent intent = new Intent(INTENT_ACTION_START_CROP);
        //安卓7.0以上版本需加Flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri originUri = CropUtil.getUriFromFile(mContext, config.authority, new File(config.originFilePath));
        intent.setDataAndType(originUri, "image/*");//可以选择图片类型, 如果是*表明所有类型的图片
        intent.putExtra("crop", true);//设置可裁剪状态
        intent.putExtra("scale", config.aspectX == config.aspectY);//裁剪时是否保留图片的比例, 这里的比例是1:1
        intent.putExtra("aspectX", config.aspectX);// X方向上的比例
        intent.putExtra("aspectY", config.aspectY);// Y方向上的比例
        intent.putExtra("outputX", config.outputX);// 裁剪区域的宽
        intent.putExtra("outputY", config.outputY);// 裁剪区域的宽
        intent.putExtra("return-data", false);// 是否将数据保留在Bitmap中返回, 返回的缩略图效果模糊
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//设置输出的格式
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(config.destFilePath)));// 裁剪后的保存路径, 这里的 URI 不需要区分
        intent.putExtra("noFaceDetection", true);// 不启用人脸识别
        startActivityForResult(intent, REQUEST_CODE_CROP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_CROP || resultCode != Activity.RESULT_OK
                || mCropCallback == null) return;

        // 回调
        mCropCallback.onCropComplete(mConfig.destFilePath);
        // 通知文件变更
        CropUtil.freshMediaStore(mContext, new File(mConfig.destFilePath));
    }

}
