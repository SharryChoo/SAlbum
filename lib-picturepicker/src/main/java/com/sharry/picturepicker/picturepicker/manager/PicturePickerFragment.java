package com.sharry.picturepicker.picturepicker.manager;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.sharry.picturepicker.picturepicker.impl.PicturePickerActivity;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 获取图片选择器 Activity 回调的 Fragment
 */
public class PicturePickerFragment extends Fragment {

    public static final String TAG = PicturePickerFragment.class.getSimpleName();

    /**
     * Activity Result 相关
     */
    public static final int REQUEST_CODE_PICKED = 0x000111;// 图片选择请求码

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
        if (resultCode != RESULT_OK || null == data || null == mPickerCallback) return;
        switch (requestCode) {
            case REQUEST_CODE_PICKED:
                ArrayList<String> paths = data.getStringArrayListExtra(
                        PicturePickerActivity.RESULT_EXTRA_PICKED_PICTURES);
                if (paths != null) {
                    mPickerCallback.onPickedComplete(paths);
                }
                break;
            default:
                break;
        }
    }

}
