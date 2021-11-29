package app.proyekakhir.core.domain.model.direction

import com.google.android.gms.maps.model.LatLng

data class DirectionData(
    val origin: LatLng,
    val destination: LatLng
)