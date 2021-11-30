package app.proyekakhir.core.util

import app.proyekakhir.core.data.source.local.LocalProperties

object Constants {
    const val KEY_PATH_KTP = 1
    const val KEY_PATH_STNK = 2
    const val TIMER_VALUE = 120000L
    const val DRIVER_AVAILABLE = 1
    const val DRIVER_NOT_AVAILABLE = 0
    const val NOTIFICATION_CHANNEL_ID = "01"
    const val NOTIFICATION_CHANNEL_NAME = "CH1"
    const val TYPE_DEPOSIT = 1
    const val TYPE_WITHDRAW = 2
    const val FILE_AUTHORITY = "app.proyekakhir.driverapp.provider"

    var pathKtp = ""
    var pathStnk = ""
    var pathFormal = ""

    const val LOCATION_REQUEST_INTERVAL = 3000L
    const val LOCATION_REQUEST_FASTEST_INTERVAL = 6000L
    const val LOCATION_REQUEST_SMALLEST_DISPLACEMENT = 10f
    const val REQUEST_CHECK_SETTINGS = 0x1
    const val MAPS_ZOOM_LEVEL = 17f
    const val DIRECTION_MODE = "driving"
    const val ROUTING_PREF = "less_driving"


    const val DRIVER_REFERENCE = "DriversLocation"
    const val DRIVER_ONLINE_REFERENCE = ".info/connected"
    const val LOCATION_CHILD = "tracksdata"
    const val ANIMATION_FAST_MILLIS = 50L
    const val ANIMATION_SLOW_MILLIS = 100L

    const val EXTRA_STATUS = "extra_status"


    //Local
    val PREF_TAG = LocalProperties::class.java.simpleName
    const val KEY_API_TOKEN = "key_api_token"
    const val KEY_FCM_TOKEN = "key_fcm_token"
    const val KEY_ID_DRIVER = "key_id"


    //status order
    const val ORDER_ACCEPTED = 4
    const val ORDER_VERIFIED = 5
    const val ORDER_FINISHED = 6

}