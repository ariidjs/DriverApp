package app.proyekakhir.driverapp.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import id.ionbit.ionalert.IonAlert

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBottomNav()
    }


    private fun initBottomNav() {
        navController = findNavController(R.id.nav_host_fragment)
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu_home)
        val menu = popupMenu.menu
        binding.bottomBar.setupWithNavController(menu, navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {

        val dialog = IonAlert(this, IonAlert.WARNING_TYPE).also {
            with(it) {
                titleText = "Apakah anda ingin keluar?"
                contentText = "Anda tidak akan bisa menerima order"
                confirmText = "Ya"
                cancelText = "Tidak"
                setConfirmClickListener {
                    finish()
                }
                setOnCancelListener {
                    dismiss()
                }
            }
        }
        dialog.show()
    }
}