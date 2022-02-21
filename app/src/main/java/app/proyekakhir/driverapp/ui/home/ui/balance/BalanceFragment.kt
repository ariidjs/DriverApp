package app.proyekakhir.driverapp.ui.home.ui.balance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.response.balance.BalanceResponse
import app.proyekakhir.core.ui.BalanceAdapter
import app.proyekakhir.core.ui.BaseFragment
import app.proyekakhir.core.util.Constants.TYPE_DEPOSIT
import app.proyekakhir.core.util.Constants.TYPE_WITHDRAW
import app.proyekakhir.core.util.convertToIDR
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentBalanceBinding
import app.proyekakhir.driverapp.util.handleResponses
import org.koin.androidx.viewmodel.ext.android.viewModel


class BalanceFragment : BaseFragment() {

    private var _binding: FragmentBalanceBinding? = null
    private val binding get() = _binding!!
    private val balanceViewModel: BalanceViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.slide_right)
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabBack.setOnClickListener { findNavController().popBackStack() }
        binding.fabRefresh.setOnClickListener { balanceViewModel.getHistoryBalance() }
        balanceViewModel.getHistoryBalance()
        balanceViewModel.balance.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    setData(response.value)
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

    private fun setData(response: BalanceResponse) {
        with(binding) {
            rvBalance.adapter = BalanceAdapter().also { it.items = response.data }
            tvBalance.text = convertToIDR(response.total_saldo.toInt())
            btnTopUp.setOnClickListener {
                val action = BalanceFragmentDirections.actionBalanceFragmentToDepositFragment(TYPE_DEPOSIT)
                findNavController().navigate(action)
            }
            btnWithdraw.setOnClickListener {
                val action = BalanceFragmentDirections.actionBalanceFragmentToDepositFragment(
                    TYPE_WITHDRAW)
                findNavController().navigate(action)
            }
        }
    }

}