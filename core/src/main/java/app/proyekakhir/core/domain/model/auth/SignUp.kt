package app.proyekakhir.core.domain.model.auth

import okhttp3.RequestBody
import java.io.File

data class SignUp(
    var name_driver: RequestBody,
    var email: RequestBody,
    var j_kelamin: RequestBody,
    var phone: RequestBody,
    var plat_kendaraan: RequestBody,
    var nik: RequestBody,
    var nomor_stnk: RequestBody,
    var photo_profile: File,
    var photo_ktp: File,
    var photo_stnk: File
)