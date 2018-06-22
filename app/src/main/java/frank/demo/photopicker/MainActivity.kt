package frank.demo.photopicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.frank.picturepicker.support.manager.picker.PicturePickerManager
import com.frank.picturepicker.support.manager.take.PictureTakeManager
import com.frank.picturepicker.widget.toolbar.GenericToolbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTitle()
        initViews()
    }

    private fun initTitle() {
        GenericToolbar.Builder(this)
                .setBackgroundColorRes(R.color.colorPrimary)
                .addTitleText("Album")
                .apply()
    }

    private fun initViews() {
        btnLaunchAlbum.setOnClickListener {
            if (TextUtils.isEmpty(etAlbumThreshold.text) || TextUtils.isEmpty(etSpanCount.text)) return@setOnClickListener
            PicturePickerManager.with(this)
                    .setThreshold(etAlbumThreshold.text.toString().toInt())
                    .setSpanCount(etSpanCount.text.toString().toInt())
                    .isShowScrollBehavior(checkbox.isChecked)
                    .setToolbarBackgroundColorRes(R.color.colorPrimary)
                    .setIndicatorSolidColorRes(R.color.colorPrimary)
                    .setIndicatorBorderColorRes(R.color.colorPrimary, android.R.color.white)
                    .setPictureLoader { context, uri, imageView ->
                        Glide.with(context).load(uri).into(imageView)
                    }
                    .start {
                        it.forEach {
                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        }
                    }
        }

        btnLaunchCamera.setOnClickListener {
            PictureTakeManager.with(this)
                    .setDestFilePath(cacheDir.absolutePath + File.separator + "test.jpg")
                    .setDestQuality(75)
                    .take {
                        Glide.with(this).load(it).into(ivPicture)
                    }
        }
    }

}
