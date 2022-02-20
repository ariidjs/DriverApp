package app.proyekakhir.driverapp.util

import android.animation.ValueAnimator
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.os.Vibrator
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import app.proyekakhir.core.util.Constants.NOTIFICATION_CHANNEL_ID
import app.proyekakhir.core.util.Constants.NOTIFICATION_CHANNEL_NAME
import app.proyekakhir.core.util.Constants.ORDER_ACCEPTED
import app.proyekakhir.core.util.Constants.ORDER_FINISHED
import app.proyekakhir.core.util.Constants.ORDER_VERIFIED
import app.proyekakhir.core.util.hide
import app.proyekakhir.core.util.show
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentHomeBinding
import app.proyekakhir.driverapp.databinding.OrderLayoutBinding
import app.proyekakhir.driverapp.ui.home.HomeActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.atan


fun Fragment.updateUIStatus(binding: FragmentHomeBinding, state: Boolean) {
    with(binding) {

        if (state) {
            btnStatus.setIconResource(R.drawable.ic_power_settings_24)
            btnStatus.text = getString(R.string.go_offline)
            btnStatus.setIconTintResource(R.color.red)
            txtStatus.text = getString(R.string.online_status)
            txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                android.R.drawable.presence_online,
                0,
                0,
                0
            )
        } else {
            btnStatus.setIconResource(android.R.drawable.presence_online)
            btnStatus.setIconTintResource(android.R.color.holo_green_light)
            btnStatus.text = getString(R.string.go_online)
            txtStatus.text = getString(R.string.offline_status)
            txtStatus.setCompoundDrawablesWithIntrinsicBounds(
                android.R.drawable.presence_offline,
                0,
                0,
                0
            )
        }
    }

}

fun polylineAnimator(): ValueAnimator {
    val valueAnimator = ValueAnimator.ofInt(0, 100)
    valueAnimator.interpolator = LinearInterpolator()
    valueAnimator.duration = 1500
    valueAnimator.repeatCount = 0
    return valueAnimator
}

fun getStoreMarkerBitmap(context: Context): Bitmap {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_store_marker)
    return Bitmap.createScaledBitmap(bitmap, 80, 80, false)
}

fun getUserMarkerBitmap(context: Context): Bitmap {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_customer)
    return Bitmap.createScaledBitmap(bitmap, 80, 80, false)
}

fun getRiderBitmap(context: Context): Bitmap {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_driver_top)
    return Bitmap.createScaledBitmap(bitmap, 150, 100, false)
}

fun driverAnimator(): ValueAnimator {
    val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
    valueAnimator.duration = 3000
    valueAnimator.interpolator = LinearInterpolator()
    return valueAnimator
}

fun getRotation(start: LatLng, end: LatLng): Float {
    val latDifference: Double = abs(start.latitude - end.latitude)
    val lngDifference: Double = abs(start.longitude - end.longitude)
    var rotation = -1F
    when {
        start.latitude < end.latitude && start.longitude < end.longitude -> {
            rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat()
        }
        start.latitude >= end.latitude && start.longitude < end.longitude -> {
            rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat()
        }
        start.latitude >= end.latitude && start.longitude >= end.longitude -> {
            rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat()
        }
        start.latitude < end.latitude && start.longitude >= end.longitude -> {
            rotation =
                (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat()
        }
    }
    return rotation
}

fun decodePoly(encoded: String): ArrayList<LatLng> {
    val poly: java.util.ArrayList<LatLng> = ArrayList()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat
        shift = 0
        result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng
        val p = LatLng(
            lat.toDouble() / 1E5,
            lng.toDouble() / 1E5
        )
        poly.add(p)
    }
    return poly
}

fun View.disableDoubleClick() {
    val delay: Long = 900
    if (!this.isClickable) {
        return
    }
    this.isClickable = false
    this.postDelayed(Runnable { this.isClickable = true }, delay)
}


fun updateSheetUI(binding: OrderLayoutBinding, state: Int) {
    val bottomSheet = BottomSheetBehavior.from(binding.rootBottom.parent as View)
    binding.rootBottom.show()
    when (state) {
        ORDER_ACCEPTED -> {
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            binding.btnConfirm.show()
            binding.btnSwipe.hide()
        }
        ORDER_VERIFIED -> {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.btnSwipe.show()
            binding.btnConfirm.hide()
        }
        ORDER_FINISHED -> {
            binding.rootBottom.hide()
            binding.btnSwipe.toggleState()
        }
    }
}


fun Fragment.showNotificationOrder() {
    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
    val sound = pref.getString(getString(R.string.pref_key_notif), getString(R.string.pref_sound1))
    val duration = pref.getString(getString(R.string.pref_key_notif_duration), getString(R.string.pref_duration_10))
    val soundId = resources.getIdentifier(sound, "raw", context?.packageName)
    val mPlayer = MediaPlayer.create(requireContext(), soundId)
    val isNotify = pref.getBoolean(getString(R.string.pref_key_notify), true)
    val vibrate = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    
    try {
        if (isNotify) mPlayer.start()

        vibrate.vibrate(longArrayOf(0, 1000, 1000), 0)
        mPlayer.isLooping = true
        CoroutineScope(Main).launch {
            delay(duration?.toLong()!!)
            mPlayer.stop()
            vibrate.cancel()
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }

    val intent = Intent(requireContext(), HomeActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    val pendingIntent = PendingIntent.getActivity(
        requireContext(), 0, intent,
        PendingIntent.FLAG_ONE_SHOT
    )

    val notificationBuilder = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Yeay! Kamu mendapat orderan")
        .setContentText("Horeee! Ada orderan masuk ayo terima!")
        .setAutoCancel(false)
        .setSound(null)
        .setVibrate(
            longArrayOf(
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000,
                1000
            )
        )
        .setContentIntent(pendingIntent)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setCategory(Notification.CATEGORY_MESSAGE)
        .setPriority(NotificationCompat.PRIORITY_MAX)
    val mNotificationManager =
        requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
        channel.enableVibration(true)
        channel.description = "Horeee! Ada orderan masuk ayo terima!"
        channel.setSound(null, null)

        channel.enableLights(true)
        channel.vibrationPattern = longArrayOf(
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000
        )
        notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
        mNotificationManager.createNotificationChannel(channel)
    }
    val notification = notificationBuilder.build()
    mNotificationManager.notify(0, notification)
}

