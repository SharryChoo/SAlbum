package com.sharry.picturepicker.picturewatcher.manager;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.sharry.picturepicker.picturewatcher.impl.PictureWatcherActivity;

import java.util.ArrayList;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 获取图片选择器 Activity 回调的 Fragment
 */
public class PictureWatcherFragment extends Fragment {

    public static final String TAG = PictureWatcherFragment.class.getSimpleName();

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE_PICKED = 0x000333;// 图片选择请求码

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
        if (resultCode != Activity.RESULT_OK || null == data || null == mWatcherCallback) return;
        switch (requestCode) {
            case REQUEST_CODE_PICKED:
                ArrayList<String> paths = data.getStringArrayListExtra(
                        PictureWatcherActivity.RESULT_EXTRA_PICKED_PICTURES);
                boolean isEnsure = data.getBooleanExtra(
                        PictureWatcherActivity.RESULT_EXTRA_IS_PICKED_ENSURE, false);
                mWatcherCallback.onWatcherPickedComplete(isEnsure, paths);
                break;
            default:
                break;
        }
    }

}
