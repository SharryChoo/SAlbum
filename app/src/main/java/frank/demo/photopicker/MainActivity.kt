package frank.demo.photopicker

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.frank.picturepicker.picturepicker.manager.PicturePickerManager
import com.frank.picturepicker.widget.toolbar.GenericToolbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private val APP_DIRECTORY = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}PicturePicker"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTitle()
        initViews()
    }

    private fun initTitle() {
        GenericToolbar.Builder(this)
                .setBackgroundColorRes(R.color.colorPrimary)
                .addTitleText(getString(R.string.app_name))
                .apply()
    }

    private fun initViews() {
        btnLaunchAlbum.setOnClickListener {
            if (TextUtils.isEmpty(etAlbumThreshold.text) || TextUtils.isEmpty(etSpanCount.text)) return@setOnClickListener
            PicturePickerManager.with(this)
                    .setThreshold(etAlbumThreshold.text.toString().toInt())// 一共选中的数量
                    .setSpanCount(etSpanCount.text.toString().toInt())// 每行展示的数目
                    .isShowScrollBehavior(checkboxAnimation.isChecked)// Behavior 动画
                    .setToolbarBackgroundColorRes(R.color.colorPrimary) // Toolbar 背景设置
                    .setIndicatorSolidColorRes(R.color.colorPrimary)// 选中指示器的颜色
                    .setIndicatorBorderColorRes(R.color.colorPrimary, android.R.color.white)// 指示器边界的颜色
                    .setPickerItemBackgroundColorRes(android.R.color.white)// 条目背景色
                    // 开启相机支持
                    .setCameraSupport(checkboxCamera.isChecked)
                    .setFileProviderAuthority("$packageName.FileProvider")
                    .setCameraDestQuality(80)
                    .setCameraDestDirectory(APP_DIRECTORY)// 相机文件存储路径
                    // 开启图片裁剪支持
                    .setCropSupport(checkboxCrop.isChecked)
                    .setCropCircle(true)
                    .setCropSize(1000, 1000)
                    .setCropDestFilePath(File(APP_DIRECTORY,
                            Date().time.toString() + ".jpg").absolutePath)// 不指定, 使用默认的路径
                    .setCropDestQuality(80)
                    // 图片加载框架注入
                    .setPictureLoader { context, uri, imageView ->
                        Glide.with(context).load(uri).into(imageView)
                    }
                    .start {
                        it.forEach {
                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

}
