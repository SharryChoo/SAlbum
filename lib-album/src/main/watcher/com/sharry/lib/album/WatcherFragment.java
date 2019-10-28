package com.sharry.lib.album;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sharry.lib.album.photoview.PhotoView;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-03 15:50
 */
public class WatcherFragment extends Fragment implements View.OnClickListener {

    static final SparseArray<WatcherFragment> ACTIVES = new SparseArray<>();
    static final Queue<WatcherFragment> IDLES = new ArrayDeque<>();

    @NonNull
    public static WatcherFragment getInstance(int position) {
        WatcherFragment instance = ACTIVES.get(position);
        if (instance == null) {
            instance = IDLES.poll();
            if (instance == null) {
                instance = new WatcherFragment();
            }
            ACTIVES.put(position, instance);
        }
        return instance;
    }

    /**
     * Widget.
     */
    private PhotoView mIvPicture;
    private ImageView mIvPlayIcon;

    private boolean mViewInitialized = false;

    /**
     * Display data source.
     */
    private MediaMeta mDataSource;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lib_album_fragment_watcher_pager, container, false);
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
        mViewInitialized = false;
        // Recycle Instance
        int indexOfValue = ACTIVES.indexOfValue(this);
        if (indexOfValue != -1) {
            ACTIVES.removeAt(indexOfValue);
        }
        // 添加到空闲队列中
        IDLES.offer(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_play_icon) {
            VideoPlayerActivity.launch(v.getContext(), mDataSource);
        }
    }

    void setDataSource(@Nullable MediaMeta mediaMeta) {
        mDataSource = mediaMeta;
        performShowDataSource();
    }

    private void initView(View view) {
        mIvPicture = view.findViewById(R.id.iv_picture);
        mIvPlayIcon = view.findViewById(R.id.iv_play_icon);
        mIvPlayIcon.setOnClickListener(this);
        mViewInitialized = true;
        performShowDataSource();
    }

    private void performShowDataSource() {
        if (mDataSource == null || !mViewInitialized) {
            return;
        }
        mIvPicture.setVisibility(View.VISIBLE);
        if (mDataSource.isPicture) {
            mIvPlayIcon.setVisibility(View.GONE);
            if (Constants.MIME_TYPE_GIF.equals(mDataSource.mimeType)) {
                Loader.loadGif(mIvPicture.getContext(), mDataSource, mIvPicture);
            } else {
                Loader.loadPicture(mIvPicture.getContext(), mDataSource, mIvPicture);
            }
        } else {
            mIvPlayIcon.setVisibility(View.VISIBLE);
            Loader.loadVideo(mIvPicture.getContext(), mDataSource, mIvPicture);
        }
    }

    /**
     * 获取 PhotoView
     */
    PhotoView getPhotoView() {
        return mIvPicture;
    }

    /**
     * 执行退出前的准备
     */
    void dismissOtherView() {
        if (mViewInitialized) {
            mIvPlayIcon.setVisibility(View.GONE);
            mIvPicture.setVisibility(View.VISIBLE);
        }
    }

}
