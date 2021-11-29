package app.proyekakhir.core.domain.model.balance

import android.net.Uri

data class DepositInput(
    val nama: String,
    val noRek: String,
    val saldo: String,
    val namaBank: String,
    val type: String,
    val image: Uri?,
)