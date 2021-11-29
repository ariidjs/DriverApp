package app.proyekakhir.core.domain.model.balance

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class Deposit(
    val nama: RequestBody,
    val noRek: RequestBody,
    val saldo: RequestBody,
    val namaBank: RequestBody,
    val type: RequestBody,
    val image: MultipartBody.Part,
)
