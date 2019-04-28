package com.sharry.picturepicker.facade;

/**
 * 图片裁剪的回调
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:07 PM
 */
public interface CropCallback {

    void onCropComplete(String path);

}
