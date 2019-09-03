package sharry.demo.picturepicker

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.sharry.lib.camera.AspectRatio
import com.sharry.lib.picturepicker.*
import com.sharry.lib.picturepicker.toolbar.SToolbar
import kotlinx.android.synthetic.main.app_activity_main.*
import java.io.File

private val APP_DIRECTORY = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}PicturePicker"

/**
 * PicturePicker 示例 Activity.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 12/6/2018 10:49 AM
 */
class MainActivity : AppCompatActivity() {

    private lateinit var pickerConfig: PickerConfig
    private lateinit var takerConfig: TakerConfig
    private lateinit var cropperConfig: CropperConfig

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
                .setFileProviderAuthority("$packageName.FileProvider")
                // 相机文件存储路径
                .setDirectoryPath(APP_DIRECTORY)
                // 拍摄后质量压缩
                .setPictureQuality(80)
                // 预览画面比例
                .setPreviewAspect(AspectRatio.of(4, 3))
                // 是否全屏预览(在比例基础上进行 CenterCrop, 保证画面不畸形)
                .setFullScreen(true)
                // 设置是否支持视频录制
                .setVideoRecord(true)
                // 设置录制最大时长
                .setMaxRecordDuration(10 * 1000)
                .build()

        cropperConfig = CropperConfig.Builder()
                .setCropDirectory(APP_DIRECTORY)
                .setCropSize(1000, 1000)
                .setCropQuality(80)
                .build()

        pickerConfig = PickerConfig.Builder()
                // 一共选中的数量
                .setThreshold(etAlbumThreshold.text.toString().toInt())
                // 每行展示的数目
                .setSpanCount(etSpanCount.text.toString().toInt())
                // Behavior 动画
                .isToolbarScrollable(cbAnimation.isChecked)
                .isFabScrollable(cbAnimation.isChecked)
                .isPickGif(cbGif.isChecked)
                .isPickVideo(cbVideo.isChecked)
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
                    .setPickerConfig(
                            pickerConfig.rebuild()
                                    .setThreshold(etAlbumThreshold.text.toString().toInt())// 一共选中的数量
                                    .setSpanCount(etSpanCount.text.toString().toInt())// 每行展示的数目
                                    .isToolbarScrollable(cbAnimation.isChecked)
                                    .isFabScrollable(cbAnimation.isChecked)
                                    .isPickGif(cbGif.isChecked)
                                    .isPickVideo(cbVideo.isChecked)
                                    .setCameraConfig(
                                            if (cbCamera.isChecked) takerConfig else null
                                    )
                                    .setCropConfig(
                                            if (cbCrop.isChecked) cropperConfig else null
                                    )
                                    .build()
                    )
                    // 图片加载框架注入
                    .setPictureLoader(object : IPictureLoaderEngine {
                        override fun loadPicture(context: Context, uri: String, imageView: ImageView) {
                            val options = RequestOptions()
                                    .override(imageView.width, imageView.height)
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            // 保证为静态图
                            Glide.with(context).asBitmap().load(uri).apply(options).into(imageView)
                        }

                        override fun loadGif(context: Context, uri: String, imageView: ImageView) {
                            Glide.with(context).load(uri).into(imageView)
                        }

                        override fun loadVideo(context: Context, uri: String, thumbnailPath: String?, imageView: ImageView) {
                            // Glide 可直接加载视频 uri 获取第一帧
                            val options = RequestOptions().override(imageView.width, imageView.height)
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            Glide.with(context).load(uri).apply(options).into(imageView)
                        }

                    })
                    .start {
                        Toast.makeText(this, it[0].toString(), Toast.LENGTH_SHORT).show()
                    }
        }
    }

}
