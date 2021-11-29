package app.proyekakhir.core.data.source.remote.network

import android.util.Log
import app.proyekakhir.core.domain.model.transaction.MessageData
import app.proyekakhir.core.domain.model.transaction.MessageStore
import app.proyekakhir.core.domain.model.transaction.MessageTrans
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
        if (data.isNotEmpty()) {
            val messageData = data["content"]
            val transaction = JSONObject(messageData!!)
            val trans = Gson().fromJson(transaction.getString("transaksi"), MessageTrans::class.java)
            val store = Gson().fromJson(transaction.getString("store"), MessageStore::class.java)
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
        }

    }

}