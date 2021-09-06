package app.proyekakhir.driverapp.ui.home.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import app.proyekakhir.core.util.Constants.DRIVER_ONLINE_REFERENCE
import app.proyekakhir.core.util.Constants.DRIVER_REFERENCE
import app.proyekakhir.core.util.Constants.MAPS_ZOOM_LEVEL
import app.proyekakhir.core.util.collapse
import app.proyekakhir.core.util.expand
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentHomeBinding
import app.proyekakhir.driverapp.util.updateUIStatus
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.shashank.sony.fancytoastlib.FancyToast
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(), OnMapReadyCallback {

    private val homeViewModel: HomeViewModel by viewModel()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var mapFragment: SupportMapFragment? = null
    private var isExpanded = false

    //Firebase Database
    private val firebaseDatabase: FirebaseDatabase by inject()
    private lateinit var onlineRef: DatabaseReference
    private lateinit var currentRef: DatabaseReference
    private lateinit var driverLocationRef: DatabaseReference
    private lateinit var geoFire: GeoFire
    private lateinit var geoQuery: GeoQuery
    private var onlineValueEventListener : ValueEventListener? = null
    private var currentValueEventListener: ValueEventListener? = null

    //Location
    private val fusedLocationProviderClient: FusedLocationProviderClient by inject()
    private var mMap: GoogleMap? = null


    private var isOnline = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i("TAG", "onAttach: ")
        initFirebase()
    }

    private fun initFirebase() {
        driverLocationRef = firebaseDatabase.getReference(DRIVER_REFERENCE)
        currentRef = firebaseDatabase.getReference(DRIVER_REFERENCE).child("1")
        onlineRef = firebaseDatabase.getReference(DRIVER_ONLINE_REFERENCE)
        val currentValueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    showToast("Anda Online!", FancyToast.SUCCESS)
                    isOnline = true
                    updateUIStatus(binding, true)
                    homeViewModel.requestLocation()
                } else {
                    Log.i("TAG", "onDataChange: not exists")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(error.message, FancyToast.ERROR)
            }
        }

        val onlineValueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    val con = currentRef.push()
                    con.onDisconnect().removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(error.message, FancyToast.ERROR)
            }
        }

        currentRef.addValueEventListener(currentValueEvent)
        onlineRef.addValueEventListener(onlineValueEvent)
        this.currentValueEventListener = currentValueEvent
        this.onlineValueEventListener = onlineValueEvent
        geoFire = GeoFire(driverLocationRef)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        Log.i("TAG", "onCreateView: ")
        collapseToolbar(binding)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
//            homeViewModel.initLocationListener(activity?.applicationContext!!)
            btnDropdown.setOnClickListener {
                if (isExpanded) collapseToolbar(this) else expandToolbar(
                    this
                )
            }
            rootStatus.setOnClickListener {
                if (isExpanded) collapseToolbar(this) else expandToolbar(
                    this
                )
            }
            btnStatus.setOnClickListener { registerOnline() }
        }
        observableData()
    }

    private fun observableData() {
        homeViewModel.location.observe(viewLifecycleOwner, { location ->
            if (location != null) {
                val lastLocation = LatLng(location.latitude, location.longitude)
                mMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(lastLocation, MAPS_ZOOM_LEVEL)
                )
                setLocationToDB(location)
                Log.i("TAG", "observableData: ${lastLocation.longitude}")
            }

        })
    }

    private fun registerOnline() {
        with(binding) {
            isOnline = if (!isOnline) {
                updateUIStatus(this, true)
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                homeViewModel.requestLocation()
                true
            } else {
                updateUIStatus(this, false)
                homeViewModel.removeLocation()
                removeLocationFromDB()
                false
            }

        }
    }

    private fun removeLocationFromDB() {
        geoFire.removeLocation("1")
//        onlineValueEventListener?.let { onlineRef.removeEventListener(it) }

    }

    private fun setLocationToDB(location: Location) {
//        currentValueEventListener?.let { currentRef.removeEventListener(it) }
        geoFire.setLocation(
            "1",
            GeoLocation(location.latitude, location.longitude)
        ) { _, error: DatabaseError? ->
            if (error != null) {
                showToast(error.code.toString(), FancyToast.ERROR)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.isMyLocationEnabled = true
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.uber_maps_style
                )
            )
            if (!success) showToast("Error Loading Maps", FancyToast.ERROR)
        } catch (e: Resources.NotFoundException) {
            showToast("Error Loading Maps", FancyToast.ERROR)
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val token = CancellationTokenSource()
        binding.fabMyLoc.setOnClickListener {
            val tasks = fusedLocationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                token.token
            ) as Task<Location>
            tasks.addOnSuccessListener {
                val currentLocation = LatLng(it.latitude, it.longitude)
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLocation,
                        MAPS_ZOOM_LEVEL
                    )
                )
            }

        }
        val task = fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            token.token
        ) as Task<Location>

        task.addOnSuccessListener { location ->
            if (location != null) {
                val lastLocation = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        lastLocation,
                        MAPS_ZOOM_LEVEL
                    )
                )
            }
        }.addOnFailureListener {
            showToast("Error getting location", FancyToast.ERROR)
        }
    }

    override fun onDestroyView() {
        _binding = null
        mapFragment?.onDestroyView()
        mMap?.clear()
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMap?.clear()
        mMap = null
        mapFragment?.onDestroy()
    }

    private fun expandToolbar(binding: FragmentHomeBinding) {
        with(binding) {
            expand(layoutExpand)
            expand(btnStatus)
            btnDropdown.setImageResource(R.drawable.ic_keyboard_arrow_up_24)
        }
        isExpanded = true
    }

    private fun collapseToolbar(binding: FragmentHomeBinding) {
        with(binding) {
            collapse(layoutExpand)
            collapse(btnStatus)
            btnDropdown.setImageResource(R.drawable.ic_expand_more_24)
        }
        isExpanded = false
    }
}