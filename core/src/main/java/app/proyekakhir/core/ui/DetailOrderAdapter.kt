package app.proyekakhir.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.proyekakhir.core.data.source.remote.response.transaction.DetailTransaksi
import app.proyekakhir.core.databinding.ItemOrderLayoutBinding

class DetailOrderAdapter :
    RecyclerView.Adapter<DetailOrderAdapter.ViewHolder>() {

    var items: List<DetailTransaksi> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = ViewHolder(
        ItemOrderLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: DetailOrderAdapter.ViewHolder, position: Int) {
        with(holder) {
            bind(items[position])
        }
    }

    override fun getItemCount() = items.size
    inner class ViewHolder(private val binding: ItemOrderLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DetailTransaksi) = with(binding) {
            data.let {
                txtNamaProduk.text = it.name_product
                txtBanyakProduk.text = StringBuilder().append("x ").append(it.count).toString()
            }
        }
    }

}