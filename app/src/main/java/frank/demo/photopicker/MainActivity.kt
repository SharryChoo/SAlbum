package frank.demo.photopicker

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.frank.lib_picturepicker.picturepicker.support.PicturePickerManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvText.setOnClickListener {
            PicturePickerManager.with(this)
                    .setThreshold(6)
                    .setSpanCount(3)
                    .setIndicatorSolidColorRes(R.color.colorAccent)
                    .setIndicatorBorderColorRes(R.color.colorAccent, android.R.color.white)
                    .start {
                        it.forEach {
                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

}
