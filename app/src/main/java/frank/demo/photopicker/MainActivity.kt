package frank.demo.photopicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.frank.lib_picturepicker.picturepicker.support.PicturePickerManager
import com.frank.lib_picturepicker.toolbar.GenericToolbar
import kotlinx.android.synthetic.main.activity_main.*

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
                .addTitleText("相册")
                .apply()
    }

    private fun initViews() {
        btnLauncher.setOnClickListener {
            if (TextUtils.isEmpty(etAlbumThreshold.text) || TextUtils.isEmpty(etSpanCount.text)) return@setOnClickListener
            PicturePickerManager.with(this)
                    .setThreshold(etAlbumThreshold.text.toString().toInt())
                    .setSpanCount(etSpanCount.text.toString().toInt())
                    .isShowScrollBehavior(checkbox.isChecked)
                    .setToolbarBackgroundColorRes(R.color.colorPrimary)
                    .setIndicatorSolidColorRes(R.color.colorPrimary)
                    .setIndicatorBorderColorRes(R.color.colorPrimary, android.R.color.white)
                    .start {
                        it.forEach {
                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

}
