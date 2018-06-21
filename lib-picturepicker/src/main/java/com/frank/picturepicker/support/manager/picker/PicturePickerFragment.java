package com.frank.picturepicker.support.manager.picker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.frank.picturepicker.support.callback.PickerCallback;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 获取图片选择器 Activity 回调的 Fragment
 */
public class PicturePickerFragment extends Fragment {

    public static final String TAG = PicturePickerFragment.class.getSimpleName();

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE_PICKED = 0x00000011;// 图片选择请求码
    public static final int REQUEST_CODE_SETTING = 0x00000022;// 设置页面请求码
    public static final int REQUEST_CODE_PERMISSIONS = 0x00000033;// 权限申请请求码

    public static final String RESULT_EXTRA_PICKED_PICTURES = "extra_picked_pictures";// 返回的图片

    public static PicturePickerFragment newInstance() {
        PicturePickerFragment fragment = new PicturePickerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private String[] mPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private PickerCallback mPickerCallback;
    private PermissionsCallback mPermissionsCallback;

    /**
     * 权限请求的接口
     */
    public interface PermissionsCallback {
        void onResult(boolean granted);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * 设置图片选择回调
     */
    public void setPickerCallback(PickerCallback callback) {
        this.mPickerCallback = callback;
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
        if (requestCode == REQUEST_CODE_PICKED && data != null && mPickerCallback != null) {
            ArrayList<String> paths = data.getStringArrayListExtra(RESULT_EXTRA_PICKED_PICTURES);
            if (paths != null) {
                mPickerCallback.onResult(paths);
            }
        } else if (requestCode == REQUEST_CODE_SETTING) {
            mPermissionsCallback.onResult(isAllGranted());
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
        AlertDialog dialog = new AlertDialog.Builder(getContext())
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
                        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                        startActivityForResult(intent, REQUEST_CODE_SETTING);
                    }
                }).create();
        dialog.show();
    }

    private boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private void log(String message) {
        Log.i(TAG, message);
    }
}
