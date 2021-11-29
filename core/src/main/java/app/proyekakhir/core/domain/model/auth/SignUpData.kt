package app.proyekakhir.core.domain.model.auth

import android.net.Uri

data class SignUpData(
    var name_driver: String,
    var email: String,
    var j_kelamin: String,
    var phone: String,
    var plat_kendaraan: String,
    var nik: String,
    var nomor_stnk: String,
    var photo_profile: Uri,
    var photo_ktp: Uri,
    var photo_stnk: Uri

)