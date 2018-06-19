package com.frank.lib_picturepicker.callback;

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
public class CallbackFragment extends Fragment {

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE = 0x00001234;// 请求码
    public static final String RESULT_EXTRA_PICKED_PICTURES = "extra_picked_pictures";// 返回的图片

    public static CallbackFragment newInstance() {
        CallbackFragment fragment = new CallbackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private Callback mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null && mCallback != null) {
            ArrayList<String> paths = data.getStringArrayListExtra(RESULT_EXTRA_PICKED_PICTURES);
            if (paths == null) return;
            mCallback.onResult(paths);
        }
    }

    /**
     * 设置回调
     *
     * @param callback
     */
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

}
