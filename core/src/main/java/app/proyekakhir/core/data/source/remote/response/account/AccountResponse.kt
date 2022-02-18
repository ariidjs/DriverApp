package app.proyekakhir.core.data.source.remote.response.account

data class AccountResponse(
    val data: AccountData,
    val message: String,
    val success: Boolean
)

data class AccountData(
    val created_at: String,
    val email: String,
    val fcm: String,
    val id: Int,
    val j_kelamin: Int,
    val name_driver: String,
    val nik: String,
    val nomor_stnk: String,
    val phone: String,
    val photo_profile: String,
    val plat_kendaraan: String,
    val rating: Int,
    val total_order: Int,
    val saldo: Int,
    val status: Int,
    val status_delete: Int,
    val updated_at: String,
    val benefit: Int
)