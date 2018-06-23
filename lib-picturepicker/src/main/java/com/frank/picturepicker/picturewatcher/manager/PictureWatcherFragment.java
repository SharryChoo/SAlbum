package com.frank.picturepicker.picturewatcher.manager;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 获取图片选择器 Activity 回调的 Fragment
 */
public class PictureWatcherFragment extends Fragment {

    public static final String TAG = PictureWatcherFragment.class.getSimpleName();

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE_PICKED = 0x00001111;// 图片选择请求码

    public static final String RESULT_EXTRA_PICKED_PICTURES = "extra_picked_pictures";// 返回的图片

    public static PictureWatcherFragment newInstance() {
        PictureWatcherFragment fragment = new PictureWatcherFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private WatcherCallback mWatcherCallback;

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
    public void setPickerCallback(WatcherCallback callback) {
        this.mWatcherCallback = callback;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICKED && data != null && mWatcherCallback != null) {
            ArrayList<String> paths = data.getStringArrayListExtra(RESULT_EXTRA_PICKED_PICTURES);
            if (paths != null) {
                mWatcherCallback.onWatcherPickedComplete(paths);
            }
        }
    }

}
