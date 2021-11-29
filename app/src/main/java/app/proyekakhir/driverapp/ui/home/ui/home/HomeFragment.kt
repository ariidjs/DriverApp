package app.proyekakhir.driverapp.ui.home.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.response.account.AccountData
import app.proyekakhir.core.data.source.remote.response.direction.DirectionResponse
import app.proyekakhir.core.data.source.remote.response.transaction.TransactionResponse
import app.proyekakhir.core.domain.model.direction.DirectionData
import app.proyekakhir.core.domain.model.driver.FirebaseData
import app.proyekakhir.core.domain.model.transaction.MessageData
import app.proyekakhir.core.ui.BaseFragment
import app.proyekakhir.core.util.Constants.DRIVER_AVAILABLE
import app.proyekakhir.core.util.Constants.DRIVER_NOT_AVAILABLE
import app.proyekakhir.core.util.Constants.DRIVER_ONLINE_REFERENCE
import app.proyekakhir.core.util.Constants.DRIVER_REFERENCE
import app.proyekakhir.core.util.Constants.LOCATION_CHILD
import app.proyekakhir.core.util.Constants.MAPS_ZOOM_LEVEL
import app.proyekakhir.core.util.Constants.ORDER_ACCEPTED
import app.proyekakhir.core.util.Constants.ORDER_FINISHED
import app.proyekakhir.core.util.Constants.ORDER_VERIFIED
import app.proyekakhir.core.util.collapse
import app.proyekakhir.core.util.convertToIDR
import app.proyekakhir.core.util.expand
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.BuildConfig
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.DialogPinLayoutBinding
import app.proyekakhir.driverapp.databinding.FragmentHomeBinding
import app.proyekakhir.driverapp.databinding.OrderLayoutBinding
import app.proyekakhir.driverapp.ui.home.ui.transaction.TransactionViewModel
import app.proyekakhir.driverapp.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : BaseFragment(), OnMapReadyCallback {

    private val homeViewModel: HomeViewModel by viewModel()
    private val transactionViewModel: TransactionViewModel by viewModel()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var mapFragment: SupportMapFragment? = null
    private var isExpanded = false
    private var accountData: AccountData? = null
    private var myLocation: LatLng? = null
    private lateinit var dialog: AlertDialog

    //Directions
    private var bluePolyLine: Polyline? = null
    private var whitePolyLine: Polyline? = null
    private var listPolyline = ArrayList<LatLng>()
    private var latLngBounds: LatLngBounds? = null
    private var storeMarker: Marker? = null
    private var buyerMarker: Marker? = null

    //Firebase Database
    private val firebaseDatabase: FirebaseDatabase by inject()
    private lateinit var onlineRef: DatabaseReference
    private lateinit var currentRef: DatabaseReference
    private lateinit var driverLocationRef: DatabaseReference
    private lateinit var geoFire: GeoFire

    //    private lateinit var geoQuery: GeoQuery
    private var onlineValueEventListener: ValueEventListener? = null
    private var currentValueEventListener: ValueEventListener? = null

    //Location
    private val fusedLocationProviderClient: FusedLocationProviderClient by inject()
    private var mMap: GoogleMap? = null


    private var isOnline = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initFirebase()
    }

    private fun initFirebase() {
        driverLocationRef = firebaseDatabase.getReference(DRIVER_REFERENCE)
//        currentRef =
//            firebaseDatabase.getReference(DRIVER_REFERENCE)
//                .child(localProperties.fcmToken!!.trimSubstring(0, 10))
        currentRef =
            firebaseDatabase.getReference(DRIVER_REFERENCE)
                .child(localProperties.idDriver.toString())
        onlineRef = firebaseDatabase.getReference(DRIVER_ONLINE_REFERENCE)
        val currentValueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isOnline = true
                    homeViewModel.requestLocation()
                    updateUIStatus(binding, true)
                } else {
                    Log.i("TAGG", "onDataChange: not exists")
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
        geoFire = GeoFire(currentRef)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        collapseToolbar(binding)

        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment?.getMapAsync(this)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.getAccount()
        with(binding) {
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

        }
        observableData()
    }

    private fun observableData() {
        homeViewModel.location.observe(viewLifecycleOwner, { location ->
            if (location != null) {
//                val lastLocation = LatLng(location.latitude, location.longitude)
//                mMap?.animateCamera(
//                    CameraUpdateFactory.newLatLngZoom(lastLocation, MAPS_ZOOM_LEVEL)
//                )
                setLocationToDB(location)
            }
        })

        homeViewModel.accounts.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {

                    accountData = response.value.data
                    if (accountData?.status == DRIVER_NOT_AVAILABLE) {
                        transactionViewModel.getCurrentOrder()
                    }
                    setProfile(response.value.data)
                }
                is Resource.Error -> {
                    handleAuth(response)
                }
                is Resource.Loading -> {
                    when (response.isLoading) {
                        true -> showLoading()
                        false -> hideLoading()
                    }
                }
            }
        })

        transactionViewModel.current.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    expandOrderSheet(response.value, response.value.transaction.status)
                }
                is Resource.Error -> {
                    handleAuth(response)
                }
                is Resource.Loading -> {
                    when (response.isLoading) {
                        true -> showLoading()
                        false -> hideLoading()
                    }
                }
            }
        })

        homeViewModel.directionStore.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.value.routes.isNotEmpty())
                        addPolyline(response.value, ORDER_ACCEPTED)
                }
                is Resource.Error -> {
                    handleAuth(response)
                }
                is Resource.Loading -> {
                    when (response.isLoading) {
                        true -> showLoading()
                        false -> hideLoading()
                    }
                }
            }
        })

        homeViewModel.directionUser.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.value.routes.isNotEmpty())
                        addPolyline(response.value, ORDER_VERIFIED)
                }
                is Resource.Error -> {
                    handleAuth(response)
                }
                is Resource.Loading -> {
                    when (response.isLoading) {
                        true -> showLoading()
                        false -> hideLoading()
                    }
                }
            }
        })
    }

    private fun addPolyline(data: DirectionResponse, states: Int) {
        for (i in data.routes.indices) {
            val polyline = data.routes[i].overview_polyline.points
            listPolyline = decodePoly(polyline)
        }
        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.WHITE)
        polylineOptions.startCap(SquareCap())
        polylineOptions.width(12f)
        polylineOptions.jointType(JointType.ROUND)
        polylineOptions.addAll(listPolyline)
        whitePolyLine = mMap?.addPolyline(polylineOptions)

        val bluePolylineOptions = PolylineOptions()
        bluePolylineOptions.color(Color.rgb(255, 189, 74))
        bluePolylineOptions.startCap(SquareCap())
        bluePolylineOptions.width(12f)
        bluePolylineOptions.jointType(JointType.ROUND)
        bluePolylineOptions.addAll(listPolyline)
        bluePolyLine = mMap?.addPolyline(bluePolylineOptions)

        val animatorValue = polylineAnimator()
        animatorValue.addUpdateListener {
            val points = whitePolyLine!!.points
            val percentValue = it.animatedValue.toString().toInt()
            val size = points.size
            val newPoint = (size * (percentValue / 100.0f)).toInt()
            val p = points.subList(0, newPoint)
            bluePolyLine!!.points = p
        }
        animatorValue.start()

        val endLoc =
            LatLng(data.routes[0].legs[0].end_location.lat, data.routes[0].legs[0].end_location.lng)
        if (states == ORDER_ACCEPTED) {
            storeMarker = mMap?.addMarker(
                MarkerOptions()
                    .position(endLoc).title("Your Destination")
                    .icon(BitmapDescriptorFactory.fromBitmap(getStoreMarkerBitmap(requireContext())))
            )
        }
        if (states == ORDER_VERIFIED) {
            buyerMarker = mMap?.addMarker(
                MarkerOptions()
                    .position(endLoc).title("Your Destination")
                    .icon(BitmapDescriptorFactory.fromBitmap(getUserMarkerBitmap(requireContext())))
            )
        }
        val latLongBuilder = LatLngBounds.Builder()
        for (item in polylineOptions.points) {
            latLongBuilder.include(item)
        }
        latLngBounds = latLongBuilder.build()
        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 2))

    }

    @SuppressLint("MissingPermission")
    private fun setProfile(data: AccountData) {
        Glide.with(requireView()).load(BuildConfig.IMG_URL + "/" + data.photo_profile)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
            .into(binding.profileImage)
        binding.txtRating.text = data.rating.toString()
        binding.txtTotalOrder.text = data.total_order.toString()
        binding.btnStatus.setOnClickListener { registerOnline() }
        val token = CancellationTokenSource()
        if (accountData?.status == DRIVER_NOT_AVAILABLE) {
            binding.fabMyLoc.setOnClickListener {
                mMap?.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        latLngBounds,
                        2
                    )
                )
            }
        }
        if (accountData?.status == DRIVER_AVAILABLE) {
            binding.fabMyLoc.setOnClickListener {
                val task = fusedLocationProviderClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    token.token
                ) as Task<Location>

                task.addOnSuccessListener { location ->
                    if (location != null) {
                        val lastLocation = LatLng(location.latitude, location.longitude)
                        myLocation = lastLocation
                        mMap?.animateCamera(
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
        }
    }

    private fun registerOnline() {

        with(binding) {
            isOnline = if (!isOnline) {
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
                accountData?.let { data ->
                    currentRef.setValue(
                        FirebaseData(
                            StringBuilder().append(myLocation?.latitude.toString()).append(", ")
                                .append(myLocation?.longitude.toString()).toString(),
                            data.id,
                            data.rating,
                            data.total_order,
                            data.status
                        )
                    )
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
        currentRef.removeValue()
        onlineValueEventListener?.let { onlineRef.removeEventListener(it) }

    }

    private fun setLocationToDB(location: Location) {
        if (accountData != null) {
            geoFire.setLocation(
                LOCATION_CHILD,
                GeoLocation(location.latitude, location.longitude)
            ) { _, error: DatabaseError? ->
                if (error != null) {
                    showToast(error.code.toString(), FancyToast.ERROR)
                }
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
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
        mMap = googleMap
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false
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


        val token = CancellationTokenSource()

        val task = fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            token.token
        ) as Task<Location>

        task.addOnSuccessListener { location ->
            if (location != null) {
                val lastLocation = LatLng(location.latitude, location.longitude)
                myLocation = lastLocation
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

    private fun updateStatus(status: Int) {
        currentRef.child("status").setValue(status)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault()
                .hasSubscriberForEvent(MessageData::class.java) && EventBus.getDefault()
                .hasSubscriberForEvent(TransactionResponse::class.java)
        ) {
            EventBus.getDefault().removeStickyEvent(MessageData::class.java)
            EventBus.getDefault().removeAllStickyEvents()
        }

        currentRef.removeEventListener(currentValueEventListener!!)

        EventBus.getDefault().unregister(this)
        mMap?.clear()
        mMap = null
        mapFragment?.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEventReceived(data: MessageData) {
        showNotificationOrder()
        val action = HomeFragmentDirections.actionNavHomeToIncomingFragment(data)
        findNavController().navigate(action)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEventReceived(data: TransactionResponse) {
        expandOrderSheet(data, data.transaction.status)
        polylineToStore(data)
    }

    private fun polylineToStore(data: TransactionResponse) {
        homeViewModel.directionStore(
            DirectionData(
                myLocation!!,
                LatLng(data.store.latitude.toDouble(), data.store.longititude.toDouble())
            )
        )
    }


    private fun expandOrderSheet(data: TransactionResponse, states: Int) {
        updateStatus(DRIVER_NOT_AVAILABLE)
        binding.btnStatus.isEnabled = false
        val orderBinding =
            OrderLayoutBinding.bind(activity?.findViewById<ConstraintLayout>(R.id.root_bottom)?.rootView!!)
        //Set data
        with(orderBinding) {
            updateSheetUI(this, states)
            btnDetailOrder.setOnClickListener {
                if (parentFragmentManager.fragments.last() is HomeFragment) {
                    val action = HomeFragmentDirections.actionNavHomeToOrderFragment(data)
                    findNavController().navigate(action)
                }
            }
            txtStoreName.text = data.store.store_name
            txtStoreAddress.text = data.store.address
            txtCustName.text = data.transaction.customer_name
            txtHargaPesanan.text = convertToIDR(data.transaction.total_price.toInt())
            txtHargaKomisi.text = convertToIDR(data.transaction.driver_price.toInt())
            btnConfirm.setOnClickListener { showVerificationDialog(data) }
            btnSwipe.setOnActiveListener {
                transactionViewModel.finishOrder(data.transaction.id)
                transactionViewModel.finish.observe(viewLifecycleOwner, { response ->
                    when (response) {
                        is Resource.Success -> {
                            updateSheetUI(this, ORDER_FINISHED)
                            listPolyline.clear()
                            bluePolyLine?.remove()
                            whitePolyLine?.remove()
                            buyerMarker?.remove()
                            binding.btnStatus.isEnabled = true
                            mMap?.clear()
                            homeViewModel.getAccount()
                            updateStatus(DRIVER_AVAILABLE)
                        }
                        is Resource.Error -> {
                            handleAuth(response)
                        }
                        is Resource.Loading -> {
                            when (response.isLoading) {
                                true -> showLoading()
                                false -> hideLoading()
                            }
                        }
                    }
                })
            }

            homeViewModel.validation.observe(viewLifecycleOwner, { response ->
                when (response) {
                    is Resource.Success -> {
                        listPolyline.clear()
                        bluePolyLine?.remove()
                        whitePolyLine?.remove()
                        storeMarker?.remove()
                        mMap?.clear()
                        dialog.dismiss()
                        homeViewModel.directionUser(
                            DirectionData(
                                myLocation!!,
                                LatLng(
                                    data.transaction.latitude.toDouble(),
                                    data.transaction.longitude.toDouble()
                                )
                            )
                        )
                        updateSheetUI(this, response.value.transaction.status)
                        showToast("Success verification code!", FancyToast.SUCCESS)
                    }
                    is Resource.Error -> {
                        handleAuth(response)
                    }
                    is Resource.Loading -> {
                        when (response.isLoading) {
                            true -> showLoading()
                            false -> hideLoading()
                        }
                    }
                }
            })
        }
        CoroutineScope(Main).launch {
            delay(2000)
            if (states == ORDER_ACCEPTED) {
                homeViewModel.directionStore(
                    DirectionData(
                        myLocation!!,
                        LatLng(data.store.latitude.toDouble(), data.store.longititude.toDouble())
                    )
                )
            } else homeViewModel.directionUser(
                DirectionData(
                    myLocation!!,
                    LatLng(
                        data.transaction.latitude.toDouble(),
                        data.transaction.longitude.toDouble()
                    )
                )
            )
        }

    }

    private fun showVerificationDialog(data: TransactionResponse) {
        val builder = AlertDialog.Builder(requireActivity())
        val view = LayoutInflater.from(requireContext()).inflate(
            R.layout.dialog_pin_layout,
            requireActivity().findViewById(android.R.id.content),
            false
        )
        val dialogBinding = DialogPinLayoutBinding.bind(view)
        builder.apply {
            setView(view)
            setCancelable(false)
        }
        dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        with(dialogBinding) {
            btnBatal.setOnClickListener { dialog.dismiss() }
            tvPinStore.setOnEditorActionListener { _, id, _ ->
                val status = false
                if (id == EditorInfo.IME_ACTION_NEXT || id == EditorInfo.IME_ACTION_DONE) {
                    if (!TextUtils.isEmpty(tvPinStore.text)) {
                        val code = tvPinStore.text?.toString()?.toUpperCase(Locale.US)
                        homeViewModel.validationCode(data.transaction.id, code!!)
                    }
                }
                status
            }
            btnVerifikasi.setOnClickListener {
                it.disableDoubleClick()
                if (!TextUtils.isEmpty(tvPinStore.text)) {
                    val code = tvPinStore.text?.toString()?.toUpperCase(Locale.US)
                    homeViewModel.validationCode(data.transaction.id, code!!)
                }
            }
        }
    }
}