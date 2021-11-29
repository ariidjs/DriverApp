package app.proyekakhir.driverapp.util

import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.util.Constants
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.ui.auth.MainActivity
import app.proyekakhir.driverapp.ui.auth.StatusActivity
import com.github.loadingview.LoadingDialog
import com.shashank.sony.fancytoastlib.FancyToast
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import id.ionbit.ionalert.IonAlert
import org.json.JSONException
import org.json.JSONObject

fun Fragment.handleAuth(response: Resource.Error) {
    when {
        response.isNetworkError -> showToast("Check your network connection", FancyToast.ERROR)

        response.errorCode == 401 -> {
            val intent = Intent(requireContext(), StatusActivity::class.java)
            intent.putExtra(Constants.EXTRA_STATUS, 401)
            requireActivity().startActivity(intent)
            requireActivity().finish()
        }

        response.errorCode == 403 -> {
            val intent = Intent(requireContext(), StatusActivity::class.java)
            intent.putExtra(Constants.EXTRA_STATUS, 403)
            requireActivity().startActivity(intent)
            requireActivity().finish()
        }

        response.errorBody != null -> {

            try {
                val errorBody = response.errorBody?.string().toString()
                val jsonObject = JSONObject(errorBody.trim { it <= ' ' })
                val errors = jsonObject.getString("message")

                if (errors.startsWith("authorized failed")) {
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }else {
                    showToast(errors, FancyToast.ERROR)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        else -> {
            showToast("Login Error! Silahkan coba lagi nanti", FancyToast.ERROR)
        }
    }


}
