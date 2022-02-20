package app.proyekakhir.driverapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.Log
import app.proyekakhir.core.data.source.local.LocalProperties
import app.proyekakhir.core.util.Constants
import app.proyekakhir.core.util.hide
import app.proyekakhir.core.util.show
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.android.inject
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var currentRef: DatabaseReference
    private lateinit var driverRef: DatabaseReference
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra("code", 0)) {
            1 -> {
                val localProperties = LocalProperties(context)
                firebaseDatabase =
                    FirebaseDatabase.getInstance("https://proyek-akhir-1b6f2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                driverRef =
                    firebaseDatabase.getReference(Constants.DRIVER_DATA_REFERENCE)
                        .child(localProperties.idDriver.toString())

                currentRef.child("status").setValue(0)
                Log.i("TAGG", "banned alarm")
            }
            2 -> {
                val localProperties = LocalProperties(context)
                firebaseDatabase =
                    FirebaseDatabase.getInstance("https://proyek-akhir-1b6f2-default-rtdb.asia-southeast1.firebasedatabase.app/")
                driverRef =
                    firebaseDatabase.getReference(Constants.DRIVER_DATA_REFERENCE)
                        .child(localProperties.idDriver.toString())

                driverRef.child("total_order").setValue(0)
                Log.i("TAGG", "RESET ALARM")
            }
        }
    }

    fun setBannedTime(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("code", 1)
        val pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0)
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 120000L,
            pendingIntent
        )
    }

    fun setReset(context: Context) {
        val calendar: Calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR,1)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("code", 2)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("code", 2)
        val pendingIntent = PendingIntent.getBroadcast(context, 2, intent, 0)
        alarmManager.cancel(pendingIntent)
    }

}