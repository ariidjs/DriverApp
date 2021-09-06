package app.proyekakhir.driverapp.util

import androidx.fragment.app.Fragment
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentHomeBinding

fun Fragment.updateUIStatus(binding: FragmentHomeBinding, state: Boolean) {
    with(binding) {

        if (state) {
            btnStatus.setIconResource(R.drawable.ic_power_settings_24)
            btnStatus.text = getString(R.string.go_offline)
            btnStatus.setIconTintResource(R.color.red)
            txtStatus.text = getString(R.string.online_status)
            txtStatus.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0)
        }else {
            btnStatus.setIconResource(android.R.drawable.presence_online)
            btnStatus.setIconTintResource(android.R.color.holo_green_light)
            btnStatus.text = getString(R.string.go_online)
            txtStatus.text = getString(R.string.offline_status)
            txtStatus.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_offline, 0, 0, 0)
        }
    }

}