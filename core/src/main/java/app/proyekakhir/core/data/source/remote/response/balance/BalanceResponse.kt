package app.proyekakhir.core.data.source.remote.response.balance

data class BalanceResponse(
    val data: List<BalanceData>,
    val message: String,
    val success: Boolean,
    val total_saldo: Double
)
data class BalanceData(
    val created_at: String,
    val id: Int,
    val id_driver: Int,
    val image: String,
    val namabank: String,
    val norek: String,
    val saldo: Double,
    val status: String,
    val type: String,
    val updated_at: String
)