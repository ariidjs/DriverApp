package app.proyekakhir.core.domain.model.transaction

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessageData(
    val idTrans: Int,
    val noTrans: String,
    val totalPrice: String,
    val priceDriver: String,
    val custAddress: String,
    val storeName: String,
    val storePhone: String,
    val storeLatitude: String,
    val storeLongitude: String,
    val storeAddress: String
): Parcelable


data class MessageTrans(
    val alamat_user: String,
    val created_at: String,
    val driver_price: String,
    val id: Int,
    val id_customer: Int,
    val id_driver: Int,
    val id_store: Int,
    val kode_validasi: String,
    val latitude: String,
    val longitude: String,
    val notransaksi: String,
    val status: Int,
    val total_price: String,
    val updated_at: String
)

data class MessageStore(
    val address: String,
    val latitude: String,
    val longititude: String,
    val phone: String,
    val store_name: String
)