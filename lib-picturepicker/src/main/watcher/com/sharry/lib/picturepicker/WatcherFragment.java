package com.sharry.lib.picturepicker;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sharry.lib.picturepicker.photoview.PhotoView;
import com.sharry.lib.picturepicker.subscaleview.ImageSource;
import com.sharry.lib.picturepicker.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
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
    private SubsamplingScaleImageView mIvHighQuality;
    private PhotoView mIvNormalQuality;
    private ImageView mIvPlayIcon;

    private Interaction mInteraction;

    private boolean mViewInitialized = false;

    /**
     * Display data source.
     */
    private MediaMeta mDataSource;

    @Override
    public void onAttach(@NonNull Context context) {
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
        mIvHighQuality = null;
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
        } else if (v.getId() == R.id.iv_picture) {
            mInteraction.onBackPressed();
        } else if (v.getId() == R.id.iv_gif) {
            mInteraction.onBackPressed();
        } else {
            // nothing.
        }
    }

    void setDataSource(@Nullable MediaMeta mediaMeta) {
        mDataSource = mediaMeta;
        performShowDataSource();
    }

    private void initView(View view) {
        // 图像加载
        mIvHighQuality = view.findViewById(R.id.iv_picture);
        mIvHighQuality.setOnClickListener(this);
        // gif 加载
        mIvNormalQuality = view.findViewById(R.id.iv_gif);
        mIvNormalQuality.setOnClickListener(this);
        // 播放按钮
        mIvPlayIcon = view.findViewById(R.id.iv_play_icon);
        mIvPlayIcon.setOnClickListener(this);
        mViewInitialized = true;
        performShowDataSource();
    }

    private void performShowDataSource() {
        if (mDataSource == null || !mViewInitialized) {
            return;
        }
        // 加载图片
        if (mDataSource.isPicture) {
            mIvPlayIcon.setVisibility(View.GONE);
            // 加载 GIF
            if (Constants.MIME_TYPE_GIF.equals(mDataSource.mimeType)) {
                mIvHighQuality.setVisibility(View.GONE);
                mIvNormalQuality.setVisibility(View.VISIBLE);
                mIvNormalQuality.setEnabled(true);
                Loader.loadGif(mIvNormalQuality.getContext(), mDataSource.path, mIvNormalQuality);
            }
            // 加载高清图
            else {
                mIvNormalQuality.setVisibility(View.GONE);
                mIvHighQuality.setVisibility(View.VISIBLE);
                mIvHighQuality.setImage(ImageSource.uri(mDataSource.path));
            }
        }
        // 加载视频缩略图
        else {
            mIvPlayIcon.setVisibility(View.VISIBLE);
            mIvNormalQuality.setVisibility(View.VISIBLE);
            mIvHighQuality.setVisibility(View.GONE);
            mIvNormalQuality.setEnabled(false);
            Loader.loadVideoThumbnails(mIvNormalQuality.getContext(), mDataSource.path, mDataSource.thumbnailPath, mIvNormalQuality);
        }
    }

    interface Interaction {

        void onBackPressed();

    }

}
