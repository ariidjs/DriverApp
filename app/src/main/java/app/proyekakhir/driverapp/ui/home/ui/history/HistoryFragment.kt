package app.proyekakhir.driverapp.ui.home.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.response.transaction.HistoryData
import app.proyekakhir.core.ui.BaseFragment
import app.proyekakhir.core.ui.HistoryAdapter
import app.proyekakhir.driverapp.databinding.FragmentHistoryBinding
import app.proyekakhir.driverapp.ui.home.ui.transaction.TransactionViewModel
import app.proyekakhir.driverapp.util.handleResponses
import org.koin.android.ext.android.inject

class HistoryFragment : BaseFragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val transactionViewModel: TransactionViewModel by inject()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionViewModel.getHistory()
        transactionViewModel.history.observe(viewLifecycleOwner, { response ->
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

    private fun setData(data: List<HistoryData>) {
        with(binding) {
            rvHistory.adapter = HistoryAdapter {
                val action = HistoryFragmentDirections.actionNavHistoryToDetailFragment(
                    it.notransaksi,
                    it.id_store
                )
                findNavController().navigate(action)
            }.also { it.items = data }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}