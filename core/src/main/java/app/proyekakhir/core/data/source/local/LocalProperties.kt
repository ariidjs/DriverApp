package app.proyekakhir.core.data.source.local

import android.content.Context
import android.content.SharedPreferences
import app.proyekakhir.core.util.Constants.KEY_API_TOKEN
import app.proyekakhir.core.util.Constants.KEY_FCM_TOKEN
import app.proyekakhir.core.util.Constants.KEY_ID_DRIVER
import app.proyekakhir.core.util.Constants.PREF_TAG

class LocalProperties constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    val apiToken = sharedPreferences.getString(KEY_API_TOKEN, "")

    val fcmToken = sharedPreferences.getString(KEY_FCM_TOKEN, "")

    val idDriver = sharedPreferences.getInt(KEY_ID_DRIVER, 0)

    fun saveApiToken(key: String, value: String) = editor.putString(key, value).apply()

    fun saveIdDriver(key: String, value: Int) = editor.putInt(key, value).apply()

    fun saveFcm(key: String, value: String) = editor.putString(key, value).apply()

    fun clearSession() {
        editor.clear()
    }
}