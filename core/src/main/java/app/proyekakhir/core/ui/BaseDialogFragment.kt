package app.proyekakhir.core.ui

import android.os.Bundle
import app.proyekakhir.core.data.source.local.LocalProperties
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import id.ionbit.ionalert.IonAlert
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseDialogFragment: BottomSheetDialogFragment() {
    private lateinit var loadingDialog: IonAlert
    @Inject
    lateinit var localProperties: LocalProperties
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = IonAlert(requireContext(), IonAlert.PROGRESS_TYPE)
            .setSpinKit("Circle")
            .setSpinColor("#FFBD4A")
    }

    fun showLoading() {
        loadingDialog.show()
    }

    fun hideLoading() {
        loadingDialog.dismiss()
    }
}