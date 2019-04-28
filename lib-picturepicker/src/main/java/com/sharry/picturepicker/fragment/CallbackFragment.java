package com.sharry.picturepicker.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sharry.picturepicker.utils.ActivityStateUtil;

/**
 * 用于回调 launchActivityForResult 的过度 Fragment
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/11/30 15:46
 */
public class CallbackFragment extends Fragment {

    public static final String TAG = CallbackFragment.class.getSimpleName();

    /**
     * 获取一个添加到 Activity 中的 Fragment 的实例
     *
     * @param bind The activity associated with this fragment.
     * @return an instance of CallbackFragment.
     */
    @Nullable
    public static CallbackFragment getInstance(@NonNull Activity bind) {
        if (ActivityStateUtil.isIllegalState(bind)) {
            return null;
        }
        CallbackFragment callbackFragment = findFragmentFromActivity(bind);
        if (callbackFragment == null) {
            callbackFragment = CallbackFragment.newInstance();
            FragmentManager fragmentManager = bind.getFragmentManager();
            fragmentManager.beginTransaction()
                    .add(callbackFragment, TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return callbackFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private static CallbackFragment findFragmentFromActivity(@NonNull Activity activity) {
        return (CallbackFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

    private static CallbackFragment newInstance() {
        return new CallbackFragment();
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
        if (null != mCallback) {
            mCallback.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 设置图片选择回调
     */
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    /**
     * The callback associated with this Fragment.
     */
    public interface Callback {

        void onActivityResult(int requestCode, int resultCode, Intent data);

    }

}
