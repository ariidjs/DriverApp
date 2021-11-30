package app.proyekakhir.driverapp.ui.home.ui.transaction

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import app.proyekakhir.core.data.source.remote.response.transaction.TransactionResponse
import app.proyekakhir.core.ui.DetailOrderAdapter
import app.proyekakhir.core.util.convertDateTime
import app.proyekakhir.core.util.convertToIDR
import app.proyekakhir.driverapp.databinding.FragmentOrderBinding
import app.proyekakhir.driverapp.util.getAllPrice
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: BottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        _binding = FragmentOrderBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(binding.root)

        arguments?.let {
            val data = OrderFragmentArgs.fromBundle(it).detailTransaction
            if (!data.equals(null)) {
                setData(data)
            }

        }
        return dialog
    }

    private fun setData(data: TransactionResponse) {
        with(binding) {
            txtOrderidDetail.text =
                StringBuilder().append("No. ").append(data.transaction.notransaksi).toString()
            txtTanggalDetail.text = convertDateTime(data.transaction.created_at, "EEEE, dd MMM, HH.mm")
            txtCustName.text = data.transaction.customer_name
            txtAlamatDetail.text = data.transaction.address_customer
            txtHargaProduk.text = convertToIDR(data.transaction.total_price.toInt())
            txtOngkir.text = convertToIDR(data.transaction.driver_price.toInt())
            txtTotalBayar.text = getAllPrice(
                data.transaction.total_price.toInt(),
                data.transaction.driver_price.toInt()
            )

            btnTelpCust.setOnClickListener {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:" + data.transaction.customer_phone)
                startActivity(intent)
            }

            rvDetailOrder.adapter = DetailOrderAdapter().also {
                it.items = data.detail_transaksi
            }
        }
    }
}