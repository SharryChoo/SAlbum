package com.frank.picturepicker.picturepicker.manager;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.frank.picturepicker.picturepicker.impl.PicturePickerActivity;

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

    public static PicturePickerFragment newInstance() {
        PicturePickerFragment fragment = new PicturePickerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private PickerCallback mPickerCallback;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICKED && data != null && mPickerCallback != null) {
            ArrayList<String> paths = data.getStringArrayListExtra(PicturePickerActivity.RESULT_INTENT_EXTRA_PICKED_PICTURES);
            if (paths != null) {
                mPickerCallback.onPickedComplete(paths);
            }
        }
    }

}
