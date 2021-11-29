package app.proyekakhir.core.data.source.remote.network

import app.proyekakhir.core.data.source.remote.response.direction.DirectionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiMapsInterface {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("key") mapsKey: String?,
        @Query("mode") mode: String?,
        @Query("transit_routing_preference") pref: String?,
        @Query("origin") origin: String?,
        @Query("destination") destination: String?
    ): DirectionResponse
}