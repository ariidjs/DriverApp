package app.proyekakhir.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.proyekakhir.core.data.source.remote.response.transaction.HistoryData
import app.proyekakhir.core.databinding.ItemHistoryLayoutBinding
import app.proyekakhir.core.util.convertDateTime
import app.proyekakhir.core.util.convertToIDR

class HistoryAdapter(private val clickListener: (HistoryData) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    var items: List<HistoryData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemHistoryLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) { bind(items[position], clickListener) }
    }

    inner class ViewHolder(private val binding: ItemHistoryLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(historyData: HistoryData, clickListener: (HistoryData) -> Unit) = with(binding) {
            tvNoTrans.text = historyData.notransaksi
            tvTotalPrice.text = convertToIDR(historyData.total_price.toInt())
            cardItemsHistory.setOnClickListener {
                clickListener(historyData)
            }
            tvTransDate.text = convertDateTime(historyData.created_at, "dd MMMM yyyy, HH:mm")
        }
    }
}
