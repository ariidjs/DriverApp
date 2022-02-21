package app.proyekakhir.driverapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.media.MediaPlayer
import android.os.Build
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import app.proyekakhir.core.domain.model.transaction.MessageData
import app.proyekakhir.core.domain.model.transaction.MessageStore
import app.proyekakhir.core.domain.model.transaction.MessageTrans
import app.proyekakhir.core.util.Constants
import app.proyekakhir.driverapp.ui.home.HomeActivity
import app.proyekakhir.driverapp.ui.home.ui.transaction.IncomingActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject


class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i("TAGG", "onMessageReceived: ${remoteMessage.data}")
        val data: MutableMap<String, String> = remoteMessage.data
//        val intent = Intent(this, IncomingActivity::class.java)
//        intent.flags = FLAG_ACTIVITY_NEW_TASK
//
//        startActivity(intent)

        if (data.isNotEmpty()) {
            val messageData = data["content"]
            val transaction = JSONObject(messageData!!)
            if (transaction.has("transaksi") && transaction.has("store")) {
                val trans =
                    Gson().fromJson(transaction.getString("transaksi"), MessageTrans::class.java)
                val store =
                    Gson().fromJson(transaction.getString("store"), MessageStore::class.java)

                if (isActivityRunning()) {
                    EventBus.getDefault().postSticky(
                        MessageData(
                            trans.id,
                            trans.notransaksi,
                            trans.total_price,
                            trans.driver_price,
                            trans.alamat_user,
                            store.store_name,
                            store.phone,
                            store.latitude,
                            store.longititude,
                            store.address
                        )
                    )
                } else {
                    val intent = Intent(this, IncomingActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra(
                        "orders", MessageData(
                            trans.id,
                            trans.notransaksi,
                            trans.total_price,
                            trans.driver_price,
                            trans.alamat_user,
                            store.store_name,
                            store.phone,
                            store.latitude,
                            store.longititude,
                            store.address
                        )
                    )
                    startActivity(intent)
                }
            } else {
                showNotification(data["title"]!!,transaction.getString("title"))
            }

        }
    }

    private fun isActivityRunning(): Boolean {
        val activityManager =
            applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val activitys = activityManager.getRunningTasks(Int.MAX_VALUE)
        var isActivityFound = false
        for (i in activitys.indices) {
            if (activitys[i].topActivity.toString().equals(
                    "ComponentInfo{app.proyekakhir.driverapp/app.proyekakhir.driverapp.ui.home.HomeActivity}",
                    ignoreCase = true
                )
            ) {
                isActivityFound = true
            }
        }
        return isActivityFound
    }
    private fun showNotification(title: String, desc: String) {
        val intent = Intent(applicationContext, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val notificationBuilder = NotificationCompat.Builder(applicationContext,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(desc)
            .setAutoCancel(false)
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
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    Constants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
            channel.enableVibration(true)
            channel.description = desc
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
            notificationBuilder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }
        val notification = notificationBuilder.build()
        mNotificationManager.notify(0, notification)
    }
}