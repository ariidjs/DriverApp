package app.proyekakhir.core.data.source.remote.response.transaction

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransactionResponse(
    val detail_transaksi: List<DetailTransaksi>,
    val message: String,
    val store: Store,
    val success: Boolean,
    val transaction: Transaction
):Parcelable

@Parcelize
data class Store(
    val address: String,
    val id_store: Int,
    val latitude: String,
    val longititude: String,
    val owner_name: String,
    val phone: String,
    val photo_store: String,
    val rating: Int,
    val store_name: String
):Parcelable

@Parcelize
data class Transaction(
    val address_customer: String,
    val driver_price: String,
    val id: Int,
    val latitude: String,
    val customer_name:String,
    val customer_phone: String,
    val longitude: String,
    val notransaksi: String,
    val status: Int,
    val total_price: String,
    val created_at: String
):Parcelable

@Parcelize
data class DetailTransaksi(
    val category: String,
    val count: Int,
    val description: String,
    val id_product: Int,
    val image1: String,
    val image2: String,
    val image3: String,
    val image4: String,
    val name_product: String,
    val price_product: Int
):Parcelable