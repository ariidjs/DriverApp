package app.proyekakhir.driverapp.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import app.proyekakhir.core.util.Constants.REQUEST_CHECK_SETTINGS
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.shashank.sony.fancytoastlib.FancyToast
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val permissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grant ->
            var isGranted = true
            for (map in grant.entries) {
                if (!map.value) isGranted = false
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                when {
                    isGranted -> {
                        getMyLocation()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                            && shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {

                        showPermissionSnackBar(
                            R.string.permission_rationale,
                            android.R.string.ok
                        ) {
                            requestPermission()
                        }

                    }
                    else -> {
                        showPermissionSnackBar(
                            R.string.permission_denied_explanation,
                            R.string.settings
                        ) {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data =
                                    Uri.fromParts("package", "app.proyekakhir.driverapp", null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        }
                    }
                }
            }
        }

    private val fusedLocationProviderClient: FusedLocationProviderClient by inject()
    private val locationRequest: LocationRequest by inject()
    private lateinit var locationCallback: LocationCallback
    private val locationSettingRequest: LocationSettingsRequest by inject()
    private val settingClient: SettingsClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSetupLocation()
    }

    private fun initSetupLocation() {
        locationCallback = object : LocationCallback() {}
    }

    private fun requestPermission() {
        permissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE
            )
        )
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) setupLocationPermission() else getMyLocation()
    }


    private fun getMyLocation() {
        //Check if GPS is turn on
        settingClient.checkLocationSettings(locationSettingRequest).apply {
            addOnSuccessListener {
                //getting fused location
                getFusedLocation()
            }
            addOnFailureListener {
                when ((it as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val re = it as ResolvableApiException
                            re.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.e("TAG", "Error")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        showToast(getString(R.string.location_setting_unknown), FancyToast.ERROR)
                    }
                }
            }

        }
    }

    private fun getFusedLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> getMyLocation()
                    Activity.RESULT_CANCELED -> getMyLocation()
                }
            }
        }
    }

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

    private fun setupLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CALL_PHONE
            )
        ) {
            showPermissionSnackBar(
                R.string.permission_rationale,
                android.R.string.ok
            ) {
                requestPermission()
            }
        } else {
            requestPermission()
        }
    }

    private fun showPermissionSnackBar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            getString(snackStrId),
            Snackbar.LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackBar.setAction(getString(actionStrId), listener)
        }
        snackBar.show()
    }
}