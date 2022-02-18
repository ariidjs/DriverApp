package app.proyekakhir.driverapp.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import app.proyekakhir.core.util.Constants
import app.proyekakhir.core.util.Constants.EXTRA_STATUS
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.ui.dialog.DialogFragment
import dagger.hilt.android.AndroidEntryPoint

class StatusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)
        intent?.let {
            if (it.getIntExtra(EXTRA_STATUS, 0) == 401) {
                findViewById<ImageView>(R.id.img_status).setImageResource(R.drawable.img_pending)
                findViewById<TextView>(R.id.tv_status_akun).text = getString(R.string.akun_status_pending)
            }
            if (it.getIntExtra(EXTRA_STATUS, 0) == 403) {
                findViewById<ImageView>(R.id.img_status).setImageResource(R.drawable.img_banned)
                findViewById<TextView>(R.id.tv_status_akun).text = getString(R.string.akun_status_banned)
            }
        }

    }
}