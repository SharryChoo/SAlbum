package com.frank.lib_picturepicker.picturepicker.support;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.frank.lib_picturepicker.picturepicker.PicturePickerActivity;

import java.util.ArrayList;

import static com.frank.lib_picturepicker.picturepicker.PicturePickerActivity.RESULT_EXTRA_PICKED_PICTURES;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 获取图片选择器 Activity 回调的 Fragment
 */
public class PicturePickerCallbackFragment extends Fragment {

    public static PicturePickerCallbackFragment newInstance() {
        PicturePickerCallbackFragment fragment = new PicturePickerCallbackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private PicturePickerCallback mCallback;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PicturePickerActivity.REQUEST_CODE && data != null && mCallback != null) {
            ArrayList<String> paths = data.getStringArrayListExtra(RESULT_EXTRA_PICKED_PICTURES);
            if (paths == null || paths.size() == 0) return;
            mCallback.onResult(paths);
        }
    }

    /**
     * 设置回调
     *
     * @param callback
     */
    public void setCallback(PicturePickerCallback callback) {
        this.mCallback = callback;
    }

}
