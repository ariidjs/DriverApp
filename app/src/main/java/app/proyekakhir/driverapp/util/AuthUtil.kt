package app.proyekakhir.driverapp.util

import android.content.Intent
import android.text.TextUtils
import androidx.fragment.app.Fragment
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.util.Constants
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.databinding.FragmentSignUpBinding
import app.proyekakhir.driverapp.ui.auth.StatusActivity
import com.shashank.sony.fancytoastlib.FancyToast

fun checkInputSignUp(binding: FragmentSignUpBinding): Boolean {
    with(binding) {
        return !TextUtils.isEmpty(edtEmail.text) && !TextUtils.isEmpty(edtNik.text) && !TextUtils.isEmpty(
            edtName.text
        ) && !TextUtils.isEmpty(
            edtNoKendaraan.text
        )&& !TextUtils.isEmpty(edtNoKendaraan.text) && !TextUtils.isEmpty(
            Constants.pathKtp
        )&& !TextUtils.isEmpty(edtNik.text) && !TextUtils.isEmpty(
            Constants.pathKtp
        )&& !TextUtils.isEmpty(edtNoStnk.text) && !TextUtils.isEmpty(
            Constants.pathKtp
        ) && !TextUtils.isEmpty(
            Constants.pathStnk
        ) && !TextUtils.isEmpty(Constants.pathFormal)
    }
}


