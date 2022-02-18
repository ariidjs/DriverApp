package app.proyekakhir.driverapp.util

import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.util.Constants
import app.proyekakhir.core.util.Constants.ERROR_UNAUTHORIZED
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.ui.auth.StatusActivity
import app.proyekakhir.driverapp.ui.dialog.DialogFragment
import com.shashank.sony.fancytoastlib.FancyToast
import org.json.JSONException
import org.json.JSONObject

fun Fragment.handleResponses(response: Resource.Error) {
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
                    val fragment = DialogFragment()
                    fragment.arguments = bundleOf("type" to ERROR_UNAUTHORIZED)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .add(android.R.id.content, fragment, fragment.tag)
                        .commit()

                } else {
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
