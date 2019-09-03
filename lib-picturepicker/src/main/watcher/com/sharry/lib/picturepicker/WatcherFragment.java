package com.sharry.lib.picturepicker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sharry.lib.picturepicker.photoview.OnPhotoTapListener;
import com.sharry.lib.picturepicker.photoview.PhotoView;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-09-03 15:50
 */
public class WatcherFragment extends Fragment {

    static WatcherFragment newInstance() {
        Bundle args = new Bundle();
        WatcherFragment fragment = new WatcherFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Widget.
     */
    private Interaction mInteraction;
    private PhotoView mIvPicture;
    private VideoView mVideoPlayer;
    private boolean mViewInitlized = false;

    /**
     * Display data source.
     */
    private MediaMeta mDataSource;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInteraction = (Interaction) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.picture_picker_fragment_watcher_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIvPicture = null;
        mVideoPlayer = null;
        mViewInitlized = false;
    }

    void setDataSource(@Nullable MediaMeta mediaMeta) {
        mDataSource = mediaMeta;
        performShowDataSource();
    }

    private void initView(View view) {
        mIvPicture = view.findViewById(R.id.iv_picture);
        mVideoPlayer = view.findViewById(R.id.video_player);
        mIvPicture.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                mInteraction.onBackPressed();
            }
        });
        mViewInitlized = true;
        performShowDataSource();
    }

    private void performShowDataSource() {
        if (mDataSource == null || !mViewInitlized) {
            return;
        }
        if (mDataSource.isPicture) {
            mVideoPlayer.setVisibility(View.GONE);
            mIvPicture.setVisibility(View.VISIBLE);
            if (Constants.MIME_TYPE_GIF.equals(mDataSource.mimeType)) {
                PictureLoader.loadGif(mIvPicture.getContext(), mDataSource.path, mIvPicture);
            } else {
                PictureLoader.loadPicture(mIvPicture.getContext(), mDataSource.path, mIvPicture);
            }
        } else {
            mIvPicture.setVisibility(View.GONE);
            mVideoPlayer.setVisibility(View.VISIBLE);
            // TODO 渲染视频
        }
    }

    PhotoView getPhotoView() {
        return mIvPicture;
    }

    interface Interaction {

        void onBackPressed();

    }

}
