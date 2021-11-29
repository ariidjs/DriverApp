package app.proyekakhir.core.data.source.remote.response.direction

data class DirectionResponse(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)