package app.proyekakhir.core.data.source.remote.response.signup

data class SignUpResponse(
    val data: SignUpResult,
    val message: String,
    val success: Boolean
)
data class SignUpResult(
    val created_at: String,
    val email: String,
    val id: Int,
    val j_kelamin: String,
    val name_driver: String,
    val nik: String,
    val nomor_stnk: String,
    val phone: String,
    val photo_profile: String,
    val plat_kendaraan: String,
    val updated_at: String
)