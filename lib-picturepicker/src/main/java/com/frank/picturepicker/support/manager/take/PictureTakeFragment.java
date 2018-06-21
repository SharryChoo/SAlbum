package com.frank.picturepicker.support.manager.take;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.frank.picturepicker.support.callback.TakeCallback;
import com.frank.picturepicker.support.config.TakeConfig;

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

    // 所需要的权限
    private String[] mPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    // 回调
    private PermissionsCallback mPermissionsCallback;
    private TakeCallback mTakeCallback;

    private Context mContext;
    // 存储系统相机拍摄的图片临时路径
    private File mTempFile;
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
    public void takePhoto(TakeConfig config, TakeCallback callback) {
        this.mConfig = config;
        this.mTakeCallback = callback;
        // 启动相机
        Intent intent = new Intent(INTENT_ACTION_START_CAMERA);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Util.getUriFromFile(mContext, mConfig.authority, getTempFile()));
        startActivityForResult(intent, REQUEST_CODE_TAKE);
    }

    /**
     * 设置权限请求接口的回调
     */
    public void verifyPermission(@NonNull PermissionsCallback callback) {
        mPermissionsCallback = callback;
        if (isMarshmallow() && !isAllGranted()) {
            requestPermissions(mPermissions, REQUEST_CODE_PERMISSIONS);
        } else {
            mPermissionsCallback.onResult(true);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CODE_PERMISSIONS) return;
        boolean isAllGranted = isAllGranted();
        if (isAllGranted) {
            mPermissionsCallback.onResult(true);
        } else {
            showPermissionDeniedDialog();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SETTING:
                mPermissionsCallback.onResult(isAllGranted());
                break;
            case REQUEST_CODE_TAKE:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        // 压缩选中的图片
                        Util.doCompress(mTempFile.getAbsolutePath(), mConfig.destFilePath, mConfig.destQuality);
                        // 回调
                        mTakeCallback.onResult(mConfig.destFilePath);
                        // 通知文件变更
                        Util.freshMediaStore(mContext, new File(mConfig.destFilePath));
                    } catch (IOException e) {
                        Log.e(TAG, "Camera take photo failed.", e);
                    } finally {
                        mTempFile.delete();
                    }
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isAllGranted() {
        boolean isAllGranted = true;
        for (String permission : mPermissions) {
            if (isGranted(permission)) {
                log("onRequestPermissionsResult: " + permission + " is Granted");
            } else {
                log("onRequestPermissionsResult: " + permission + " is Denied");
                isAllGranted = false;
            }
        }
        return isAllGranted;
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean isGranted(String permission) {
        return getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showPermissionDeniedDialog() {
        //启动当前App的系统设置界面
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("帮助")
                .setMessage("当前应用缺少必要权限")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPermissionsCallback.onResult(false);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 启动当前App的系统设置界面
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                        startActivityForResult(intent, REQUEST_CODE_SETTING);
                    }
                }).create();
        dialog.show();
    }

    private boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private File getTempFile() {
        if (mTempFile != null) return mTempFile;
        // 创建临时文件
        try {
            mTempFile = new File(mContext.getCacheDir(), "temp_camera.jpg");
            if (mTempFile.exists()) mTempFile.delete();
            mTempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mTempFile;
    }

    private void log(String message) {
        Log.i(TAG, message);
    }

}
