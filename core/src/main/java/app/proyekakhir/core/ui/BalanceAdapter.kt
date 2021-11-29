package app.proyekakhir.core.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.proyekakhir.core.data.source.remote.response.balance.BalanceData
import app.proyekakhir.core.databinding.ItemBalanceBinding
import app.proyekakhir.core.util.convertDate
import app.proyekakhir.core.util.convertToIDR
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class BalanceAdapter :
    RecyclerView.Adapter<BalanceAdapter.ViewHolder>() {

    var items: List<BalanceData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemBalanceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) { bind(items[position]) }
    }

    inner class ViewHolder(private val binding: ItemBalanceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(balanceData: BalanceData) = with(binding) {
            tvType.text = balanceData.type
            tvBalance.text = convertToIDR(balanceData.saldo)
            tvStatus.text = balanceData.status
            tvDate.text = convertDate(balanceData.created_at)
        }
    }
}