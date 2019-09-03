package com.sharry.lib.picturepicker;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * ViewPager 嵌套 Fragment 组合的 Adapter
 *
 * @author Frank <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2017/2/20 9:10
 */
class WatcherPagerAdapter extends FragmentStatePagerAdapter {

    private final List<? extends MediaMeta> mDataSet;
    private final SparseArray<WatcherFragment> mActives = new SparseArray<>();
    private final Queue<WatcherFragment> mIdles = new ArrayDeque<>();

    WatcherPagerAdapter(FragmentManager fragmentManager, List<? extends MediaMeta> dataSet) {
        super(fragmentManager);
        this.mDataSet = dataSet;
    }

    @Override
    public WatcherFragment getItem(int position) {
        WatcherFragment watcherFragment = mActives.get(position);
        if (watcherFragment == null) {
            watcherFragment = mIdles.poll();
            if (watcherFragment == null) {
                watcherFragment = WatcherFragment.newInstance();
            }
            mActives.put(position, watcherFragment);
        }
        watcherFragment.setDataSource(mDataSet.get(position));
        return watcherFragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
        mActives.remove(position);
        if (object instanceof WatcherFragment) {
            mIdles.offer((WatcherFragment) object);
        }
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

}
