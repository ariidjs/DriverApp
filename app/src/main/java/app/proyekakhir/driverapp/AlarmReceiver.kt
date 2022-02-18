package app.proyekakhir.driverapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import app.proyekakhir.core.data.source.local.LocalProperties
import app.proyekakhir.core.util.Constants
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.android.inject
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var currentRef: DatabaseReference
    override fun onReceive(context: Context, intent: Intent) {
        val localProperties = LocalProperties(context)
        firebaseDatabase = FirebaseDatabase.getInstance("https://proyek-akhir-1b6f2-default-rtdb.asia-southeast1.firebasedatabase.app/")
        currentRef =
            firebaseDatabase.getReference(Constants.DRIVER_REFERENCE)
                .child(localProperties.idDriver.toString())

        currentRef.child("status").setValue(0)
        Log.i("TAGG", "onReceive: ")
    }

    fun setBannedTime(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0)
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 120000L, pendingIntent)
    }

}