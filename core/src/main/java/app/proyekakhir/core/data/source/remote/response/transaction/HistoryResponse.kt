package app.proyekakhir.core.data.source.remote.response.transaction

data class HistoryResponse(
    val data: List<HistoryData>,
    val message: String,
    val success: Boolean
)

data class HistoryData(
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