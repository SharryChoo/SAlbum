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
    private val pictureLoader = object : IPictureLoaderEngine {

        override fun loadPicture(context: Context, uri: String, imageView: ImageView) {
            // 保证为静态图
            Glide.with(context).asBitmap().load(uri).into(imageView)
        }

        override fun loadGif(context: Context, uri: String, imageView: ImageView) {
            // 保证为 GIF 图
            Glide.with(context).asGif().load(uri).into(imageView)
        }

        override fun loadVideo(context: Context, uri: String, thumbnailPath: String?, imageView: ImageView) {
            // Glide 可直接加载视频 uri 获取第一帧
            Glide.with(context).asBitmap().load(uri).into(imageView)
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
                // 相机文件存储路径
                .setDirectoryPath(APP_DIRECTORY)
                // 拍摄后质量压缩
                .setPictureQuality(80)
                // 预览画面比例
                .setPreviewAspect(AspectRatio.of(4, 3))
                // 是否全屏预览(在比例基础上进行 CenterCrop, 保证画面不畸形)
                .setFullScreen(false)
                // 设置是否支持视频录制
                .setVideoRecord(true)
                // 设置录制最大时长
                .setMaxRecordDuration(30 * 1000)
                .build()

        cropperConfig = CropperConfig.Builder()
                // 指定 FileProvider 的 authority, 用于 7.0 获取文件 URI
                .setFileProviderAuthority("$packageName.FileProvider")
                // 裁剪后文件输出的路径
                .setCropDirectory(APP_DIRECTORY)
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
                    .setPictureLoader(pictureLoader)
                    .start {
                        Toast.makeText(this, it[0].toString(), Toast.LENGTH_SHORT).show()
                    }
        }
    }

}
