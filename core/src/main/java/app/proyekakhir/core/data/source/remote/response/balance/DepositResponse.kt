package app.proyekakhir.core.data.source.remote.response.balance

data class DepositResponse(
    val data: DepositData,
    val jwt: Any,
    val message: String,
    val success: Boolean
)
data class DepositData(
    val created_at: String,
    val id: Int,
    val id_driver: Int,
    val image: String,
    val nama: String,
    val namabank: String,
    val norek: String,
    val saldo: String,
    val type: String,
    val updated_at: String
)