package app.proyekakhir.driverapp.util

import app.proyekakhir.core.util.convertToIDR

fun getAllPrice(biayaProduks: Int, biayaDriver: Int): String {
    val total: String
    val jumlah: Int = biayaProduks + biayaDriver
    total = convertToIDR(jumlah)
    return total
}