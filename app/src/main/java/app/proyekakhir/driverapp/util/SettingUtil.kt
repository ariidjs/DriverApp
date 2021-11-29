package app.proyekakhir.driverapp.util

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import app.proyekakhir.core.data.source.local.LocalProperties
import app.proyekakhir.core.util.Constants
import app.proyekakhir.driverapp.ui.auth.MainActivity
import com.google.firebase.database.FirebaseDatabase
import id.ionbit.ionalert.IonAlert
import okhttp3.internal.trimSubstring

fun Activity.logout(localProperties: LocalProperties,firebaseDatabase: FirebaseDatabase) {
    val currentRef =
        firebaseDatabase.getReference(Constants.DRIVER_REFERENCE)
            .child(localProperties.fcmToken!!.trimSubstring(0, 10))
    currentRef.removeValue().addOnSuccessListener {
        localProperties.clearSession()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

fun Fragment.showLogoutDialog(localProperties: LocalProperties,firebaseDatabase: FirebaseDatabase) {
    val dialog = IonAlert(requireContext(), IonAlert.WARNING_TYPE).also {
        with(it) {
            titleText = "Apakah anda yakin untuk logout?"
            contentText = "Anda tidak akan bisa menerima order"
            confirmText = "Ya"
            cancelText = "Tidak"
            setConfirmClickListener {
                requireActivity().logout(localProperties,firebaseDatabase)
            }
            setOnCancelListener {
                dismiss()
            }
        }
    }
    dialog.show()
}