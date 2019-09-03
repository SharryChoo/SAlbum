package com.sharry.lib.picturepicker;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * ViewPager 嵌套 Fragment 组合的 Adapter
 *
 * @author Frank <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2017/2/20 9:10
 */
class WatcherPagerAdapter extends FragmentStatePagerAdapter {

    private final List<? extends MediaMeta> mDataSet;

    WatcherPagerAdapter(FragmentManager fragmentManager, List<? extends MediaMeta> dataSet) {
        super(fragmentManager);
        this.mDataSet = dataSet;
    }

    @Override
    public WatcherFragment getItem(int position) {
        WatcherFragment watcherFragment = WatcherFragment.getInstance(position);
        watcherFragment.setDataSource(mDataSet.get(position));
        return WatcherFragment.getInstance(position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

}
