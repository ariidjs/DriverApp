package app.proyekakhir.driverapp.ui.home.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.response.transaction.TransactionResponse
import app.proyekakhir.core.ui.BaseFragment
import app.proyekakhir.core.ui.DetailOrderAdapter
import app.proyekakhir.core.util.convertDateTime
import app.proyekakhir.core.util.convertToIDR
import app.proyekakhir.driverapp.databinding.FragmentDetailBinding
import app.proyekakhir.driverapp.util.getAllPrice
import app.proyekakhir.driverapp.util.handleAuth
import org.koin.android.ext.android.inject

class DetailFragment : BaseFragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by inject()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabBack.setOnClickListener { findNavController().popBackStack() }
        arguments?.let {
            val noTrans = DetailFragmentArgs.fromBundle(it).noTrans
            viewModel.getDetail(noTrans)
        }
        viewModel.detail.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    setData(response.value)
                }
                is Resource.Error -> {
                    handleAuth(response)
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

    private fun setData(data: TransactionResponse) {
        with(binding) {
            tvStoreName.text = data.store.store_name
            tvDate.text = convertDateTime(data.transaction.created_at, "EEEE, dd MMM, HH.mm")
            tvOrderNumber.text = StringBuilder().append("# ").append(data.transaction.notransaksi).toString()
            txtHargaProduk.text = convertToIDR(data.transaction.total_price.toInt())
            txtOngkir.text = convertToIDR(data.transaction.driver_price.toInt())
            txtTotalBayar.text = getAllPrice(
                data.transaction.total_price.toInt(),
                data.transaction.driver_price.toInt()
            )

            rvDetailOrder.adapter = DetailOrderAdapter().also {
                it.items = data.detail_transaksi
            }
        }
    }
}