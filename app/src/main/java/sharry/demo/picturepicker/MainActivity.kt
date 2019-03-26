package sharry.demo.picturepicker

import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.sharry.picturepicker.camera.CameraConfig
import com.sharry.picturepicker.crop.CropConfig
import com.sharry.picturepicker.picker.PickerConfig
import com.sharry.picturepicker.picker.PicturePickerManager
import com.sharry.picturepicker.widget.toolbar.SToolbar
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
                .setThreshold(etAlbumThreshold.text.toString().toInt())// 一共选中的数量
                .setSpanCount(etSpanCount.text.toString().toInt())// 每行展示的数目
                .isToolbarScrollable(cbAnimation.isChecked)// Behavior 动画
                .isFabScrollable(cbAnimation.isChecked)
                .setToolbarBackgroundColor(
                        ContextCompat.getColor(this, R.color.colorPrimary)
                ) // Toolbar 背景设置
                .setIndicatorSolidColor(
                        ContextCompat.getColor(this, R.color.colorPrimary)
                )// 选中指示器的颜色
                .setIndicatorBorderColor(
                        ContextCompat.getColor(this, R.color.colorPrimary),
                        ContextCompat.getColor(this, android.R.color.white)
                )// 指示器边界的颜色
                .setPickerItemBackgroundColor(
                        ContextCompat.getColor(this, android.R.color.white)
                )// 条目背景色
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
