package app.proyekakhir.driverapp

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import app.proyekakhir.core.domain.model.transaction.MessageData
import app.proyekakhir.core.domain.model.transaction.MessageStore
import app.proyekakhir.core.domain.model.transaction.MessageTrans
import app.proyekakhir.driverapp.ui.home.ui.transaction.IncomingActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
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

}