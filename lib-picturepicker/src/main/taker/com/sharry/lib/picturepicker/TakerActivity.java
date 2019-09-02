package com.sharry.lib.picturepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sharry.lib.camera.AspectRatio;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02
 */
public class TakerActivity extends AppCompatActivity implements ITakerContract.IView, AspectRatioFragment.Listener {


    private static final String EXTRA_TAKER_CONFIG = "extra_taker_config";

    public static void launch(Activity context, TakerConfig config) {
        Intent intent = new Intent(context, TakerActivity.class);
        intent.putExtra(EXTRA_TAKER_CONFIG, config);
        context.startActivity(intent);
    }

    ITakerContract.IPresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_picker_activity_taker);
        mPresenter = createPresenter();
        initViews();
        initData();
    }

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {

    }

    private void initViews() {

    }

    private void initData() {

    }

    private ITakerContract.IPresenter createPresenter() {
        return new TakerPresenter((TakerConfig) getIntent().getParcelableExtra(EXTRA_TAKER_CONFIG));
    }

}
