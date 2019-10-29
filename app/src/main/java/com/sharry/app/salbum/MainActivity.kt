package com.sharry.app.salbum

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.sharry.lib.album.*
import com.sharry.lib.album.TakerConfig.ASPECT_4_3
import com.sharry.lib.album.toolbar.SToolbar
import com.sharry.lib.media.recorder.Options
import kotlinx.android.synthetic.main.app_activity_main.*


/**
 * SAlbum 示例 Activity.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 12/6/2018 10:49 AM
 */
class MainActivity : AppCompatActivity() {

    private val RELATIVE_PATH = "SAlbum"

    private lateinit var pickerConfig: PickerConfig
    private lateinit var takerConfig: TakerConfig
    private lateinit var cropperConfig: CropperConfig
    private val pictureLoader = object : ILoaderEngine {
        override fun loadPicture(context: Context, mediaMeta: MediaMeta, imageView: ImageView) {
            Glide.with(context).asBitmap().load(mediaMeta.contentUri).into(imageView)
        }

        override fun loadGif(context: Context, mediaMeta: MediaMeta, imageView: ImageView) {
            Glide.with(context).asGif().load(mediaMeta.contentUri).into(imageView)
        }

        override fun loadVideoThumbnails(context: Context, mediaMeta: MediaMeta, imageView: ImageView) {
            Glide.with(context).asBitmap().load(mediaMeta.contentUri).into(imageView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_main)
        initTitle()
        initViews()
        initData()
    }

    private fun initTitle() {
        SToolbar.Builder(this)
                .setBackgroundColorRes(R.color.colorPrimary)
                .setTitleText(getString(R.string.app_name))
                .apply()
    }

    private fun initData() {
        takerConfig = TakerConfig.Builder()
                // 指定 FileProvider 的 authority, 用于 7.0 获取文件 URI
                .setAuthority("$packageName.FileProvider")
                // 预览画面比例
                .setPreviewAspect(ASPECT_4_3)
                // 是否全屏预览(在比例基础上进行 CenterCrop, 保证画面不畸形)
                .setFullScreen(true)
                // 设置自定义 Renderer 的路径
                .setRenderer(WatermarkPreviewerRenderer::class.java)
                // 设置是否支持视频录制
                .setVideoRecord(true)
                // 设置录制最大时长
                .setMaxRecordDuration(15 * 1000)
                // 设置录制最短时长
                .setMinRecordDuration(1 * 1000)
                // 设置录制的分辨率
                .setRecordResolution(Options.Video.RESOLUTION_1080P)
                // 设置外部存储目录相对路径
                .setRelativePath(RELATIVE_PATH)
                // 拍摄后质量压缩
                .setPictureQuality(80)
                .build()

        cropperConfig = CropperConfig.Builder()
                // 指定 FileProvider 的 authority, 用于 7.0 获取文件 URI
                .setAuthority("$packageName.FileProvider")
                // 设置外部存储目录相对路径
                .setRelativePath(RELATIVE_PATH)
                // 裁剪期望的尺寸
                .setCropSize(1000, 1000)
                // 裁剪后的质量
                .setCropQuality(80)
                .build()

        pickerConfig = PickerConfig.Builder()
                // Toolbar 背景设置
                .setToolbarBackgroundColor(
                        ContextCompat.getColor(this, R.color.colorPrimary)
                )
                // 指示器填充色
                .setIndicatorSolidColor(
                        ContextCompat.getColor(this, R.color.colorPrimary)
                )
                // 选中指示器的颜色
                .setIndicatorBorderColor(
                        ContextCompat.getColor(this, R.color.colorPrimary),
                        ContextCompat.getColor(this, android.R.color.white)
                )
                // 指示器边界的颜色
                .setPickerItemBackgroundColor(
                        ContextCompat.getColor(this, android.R.color.white)
                )
                // 条目背景色
                .build()
    }

    private fun initViews() {
        btnLaunchAlbum.setOnClickListener { _ ->
            if (TextUtils.isEmpty(etAlbumThreshold.text) || TextUtils.isEmpty(etSpanCount.text)) {
                return@setOnClickListener
            }
            PickerManager.with(this)
                    // 设置选择配置文件
                    .setPickerConfig(
                            pickerConfig.rebuild()
                                    // 阈值
                                    .setThreshold(etAlbumThreshold.text.toString().toInt())
                                    // 每行展示的数量
                                    .setSpanCount(etSpanCount.text.toString().toInt())
                                    // 是否开启 Toolbar Behavior 动画
                                    .isToolbarScrollable(cbAnimation.isChecked)
                                    // 是否开启 Fab Behavior 动画
                                    .isFabScrollable(cbAnimation.isChecked)
                                    // 是否选择 GIF 图
                                    .isPickGif(cbGif.isChecked)
                                    // 是否选择视频
                                    .isPickVideo(cbVideo.isChecked)
                                    // 设置相机配置, 非 null 说明支持相机(拍摄/录制)
                                    .setCameraConfig(
                                            if (cbCamera.isChecked) takerConfig else null
                                    )
                                    // 设置裁剪配置, 非 null 说明支持裁剪
                                    .setCropConfig(
                                            if (cbCrop.isChecked) cropperConfig else null
                                    )
                                    .build()
                    )
                    // 图片加载框架注入
                    .setLoaderEngine(pictureLoader)
                    .start {
                        // TODO 选中的资源, 通过 ArrayList<MediaMeta> 返回
                    }
        }
    }

}
