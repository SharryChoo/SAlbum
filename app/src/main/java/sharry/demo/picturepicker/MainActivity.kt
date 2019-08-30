package sharry.demo.picturepicker

import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.sharry.lib.picturepicker.CameraConfig
import com.sharry.lib.picturepicker.CropConfig
import com.sharry.lib.picturepicker.PickerConfig
import com.sharry.lib.picturepicker.PicturePickerManager
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
    private lateinit var cameraConfig: CameraConfig
    private lateinit var cropConfig: CropConfig

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
        cameraConfig = CameraConfig.Builder()
                .setFileProviderAuthority("$packageName.FileProvider")  // 指定 FileProvider 的 authority, 用于 7.0 获取文件 URI
                .setCameraDirectory(APP_DIRECTORY)                      // 相机文件存储路径
                .setCameraQuality(80)
                .build()

        cropConfig = CropConfig.Builder()
                .setCropDirectory(APP_DIRECTORY)                         // 裁剪文件存储路径
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
            PicturePickerManager.with(this)
                    .setPickerConfig(
                            pickerConfig.rebuild()
                                    .setThreshold(etAlbumThreshold.text.toString().toInt())// 一共选中的数量
                                    .setSpanCount(etSpanCount.text.toString().toInt())// 每行展示的数目
                                    .isToolbarScrollable(cbAnimation.isChecked)
                                    .isFabScrollable(cbAnimation.isChecked)
                                    .setCameraConfig(
                                            if (cbCamera.isChecked) cameraConfig else null
                                    )
                                    .setCropConfig(
                                            if (cbCrop.isChecked) cropConfig else null
                                    )
                                    .build()
                    )
                    // 图片加载框架注入
                    .setPictureLoader { context, uri, imageView ->
                        val options = RequestOptions()
                                .override(imageView.width, imageView.height)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        Glide.with(context)
                                .load(uri)
                                .apply(options)
                                .into(imageView)
                    }
                    .start {
                        it.forEach { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                    }
        }
    }

}
