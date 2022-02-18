package app.proyekakhir.core.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.proyekakhir.core.data.source.local.LocalProperties
import dagger.hilt.android.AndroidEntryPoint
import id.ionbit.ionalert.IonAlert
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    lateinit var loadingDialog: IonAlert
    @Inject
    lateinit var localProperties: LocalProperties
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = IonAlert(this, IonAlert.PROGRESS_TYPE)
            .setSpinKit("Circle")
            .setSpinColor("#FFBD4A")

    }
}