package app.proyekakhir.driverapp.ui.home.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.response.account.AccountData
import app.proyekakhir.core.ui.BaseFragment
import app.proyekakhir.core.util.convertToIDR
import app.proyekakhir.driverapp.BuildConfig
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentSettingBinding
import app.proyekakhir.driverapp.ui.home.ui.home.HomeViewModel
import app.proyekakhir.driverapp.util.handleResponses
import app.proyekakhir.driverapp.util.showLogoutDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingFragment : BaseFragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModel()
    private val firebaseDatabase: FirebaseDatabase by inject()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.getAccount()

        homeViewModel.accounts.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    setData(response.value.data)
                }
                is Resource.Error -> {
                    handleResponses(response)
                }
                is Resource.Loading -> {
                    when (response.isLoading) {
                        true -> showLoading()
                        false -> hideLoading()
                    }
                }
            }
        })
    }

    private fun setData(data: AccountData) {
        Glide.with(requireView()).load(BuildConfig.IMG_URL + "/" + data.photo_profile)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
            .into(binding.imgProfile)
        binding.tvAccountName.text = data.name_driver
        binding.tvAccountPhone.text = data.phone
        binding.tvVehicleNumber.text = data.plat_kendaraan
        binding.tvBalance.text = convertToIDR(data.saldo.toInt())
        binding.tvPendapatan.text = convertToIDR(data.benefit)
        binding.btnLogout.setOnClickListener {
            showLogoutDialog(localProperties, firebaseDatabase)
        }
        binding.btnSaldo.setOnClickListener {
            findNavController().navigate(R.id.action_nav_settings_to_balanceFragment)
        }
        binding.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.action_nav_settings_to_notificationFragment2)
        }
    }

}