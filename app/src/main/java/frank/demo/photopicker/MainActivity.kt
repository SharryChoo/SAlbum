package frank.demo.photopicker

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.frank.picturepicker.picturepicker.manager.PicturePickerManager
import com.frank.picturepicker.pricturecrop.manager.PictureCropManager
import com.frank.picturepicker.widget.toolbar.GenericToolbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

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
                    .setFileProviderAuthority("$packageName.FileProvider")// 指定 FileProvider 的 authority, 用于 7.0 获取文件 URI
                    .setCameraDirectory(APP_DIRECTORY)// 相机文件存储路径
                    .setCameraQuality(80)
                    // 开启图片裁剪支持
                    .setCropSupport(checkboxCrop.isChecked)
                    .setCropDirectory(APP_DIRECTORY)// 裁剪文件存储路径
                    .setCropSize(1000, 1000)
                    .setCropQuality(80)
                    // 图片加载框架注入
                    .setPictureLoader { context, uri, imageView -> Glide.with(context).load(uri).into(imageView) }
                    .start { it.forEach { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() } }
        }
    }

}
