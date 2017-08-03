package frank.demo.photopicker.adapter;

import android.support.design.widget.CheckableImageButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import frank.demo.photopicker.R;
import frank.demo.photopicker.activity.PicturePickerActivity;
import frank.demo.photopicker.activity.PicturePickerPresenter;
import frank.demo.photopicker.app_manager.MyApp;

/**
 * Created by 99538 on 2017/7/25.
 */
public class ActivityPicturePickerAdapter extends RecyclerView.Adapter {

    private List<String> mList;
    private PicturePickerPresenter mPresenter;
    private int mImageSize;

    public ActivityPicturePickerAdapter(List<String> list, PicturePickerPresenter presenter) {
        mList = list;
        mPresenter = presenter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mImageSize = parent.getMeasuredWidth() / 3;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_picture_picker, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder vh = (ViewHolder)holder;
        final String uri = mList.get(position);
        Glide.with(MyApp.getContext()).load(uri).into(vh.mImageView);
        //通过遍历已选中图片的列表来设置mPick的状态
        vh.mPick.setChecked(mPresenter.fetchPickedList().contains(uri));
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vh.mPick.isChecked()){
                    vh.mPick.setChecked(false);
                    //回调给Activity已移除该图片
                    mPresenter.pictureRemove(uri);
                } else if(!vh.mPick.isChecked() && mPresenter.fetchPickedList().size() < PicturePickerActivity.MAX_PICKED_COUNT) {
                    vh.mPick.setChecked(true);
                    //回调给Activity已选中了该图片
                    mPresenter.picturePicked(uri);
                }else {
                    Toast.makeText(MyApp.getContext(), "图片选择达到最大上限", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @MyApp.ViewResId(R.id.image) private ImageView mImageView;
        @MyApp.ViewResId(R.id.image_pick) private CheckableImageButton mPick;
        private View itemView;
        public ViewHolder(View itemView) {
            super(itemView);
            MyApp.ViewResId(this, itemView);
            this.itemView = itemView;
            //根据父类布局的宽度, 动态的设置ImageView的尺寸
            mImageView.setLayoutParams(new FrameLayout.LayoutParams(mImageSize, mImageSize));
            mPick.setChecked(false);
        }
    }
}
