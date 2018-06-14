package frank.demo.photopicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.frank.lib_picturepicker.picturepicker.support.PicturePickerManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvText.setOnClickListener {
            PicturePickerManager.with(this)
                    .setThreshold(9)
                    .setIndicatorSolidColorRes(R.color.colorAccent)
                    .setIndicatorBorderColorRes(R.color.colorAccent, android.R.color.white)
                    .start {

                    }
        }
    }

}
