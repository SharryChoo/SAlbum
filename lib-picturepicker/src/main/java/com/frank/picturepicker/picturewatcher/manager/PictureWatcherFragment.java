package com.frank.picturepicker.picturewatcher.manager;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.frank.picturepicker.picturewatcher.impl.PictureWatcherActivity;

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

    public static PictureWatcherFragment newInstance() {
        PictureWatcherFragment fragment = new PictureWatcherFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private WatcherCallback mWatcherCallback;

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
            ArrayList<String> paths = data.getStringArrayListExtra(PictureWatcherActivity.RESULT_EXTRA_PICKED_PICTURES);
            boolean isEnsure = data.getBooleanExtra(PictureWatcherActivity.RESULT_EXTRA_IS_PICKED_ENSURE, false);
            if (paths != null) {
                mWatcherCallback.onWatcherPickedComplete(isEnsure, paths);
            }
        }
    }

}
