package app.proyekakhir.driverapp.ui.home.ui.settings

import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import app.proyekakhir.driverapp.R


class PreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.base_pref, rootKey)
        val typeSound =
            findPreference<ListPreference>(getString(R.string.pref_key_notif))

        val durationSound = findPreference<ListPreference>(getString(R.string.pref_key_notif_duration))

        val prefNotification =
            findPreference<SwitchPreference>(getString(R.string.pref_key_notify))
        val mAudioManager = requireContext().getSystemService(AUDIO_SERVICE) as AudioManager
        prefNotification?.setOnPreferenceChangeListener { _, newValue ->

            when (newValue) {
                true -> {
                    typeSound?.isEnabled = true
                    durationSound?.isEnabled = true
                    mAudioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC, // Stream type
                        12, // Index
                        AudioManager.FLAG_SHOW_UI // Flags
                    )
                }
                false -> {
                    durationSound?.isEnabled = false
                    typeSound?.isEnabled = false
                }
            }
            true
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        pref.getBoolean(getString(R.string.pref_key_notify), true).apply {
            when (this) {
                true -> {
                    typeSound?.isEnabled = true
                    durationSound?.isEnabled = true
                }
                false -> {
                    durationSound?.isEnabled = false
                    typeSound?.isEnabled = false
                }
            }
        }

        typeSound?.setOnPreferenceChangeListener { _, newValue ->
            val soundId = resources.getIdentifier(newValue.toString(), "raw", context?.packageName)
            MediaPlayer.create(requireContext(), soundId).start()
            true
        }

    }
}