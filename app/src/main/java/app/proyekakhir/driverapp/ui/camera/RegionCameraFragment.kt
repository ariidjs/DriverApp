package app.proyekakhir.driverapp.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.util.*
import app.proyekakhir.core.util.Constants.ANIMATION_FAST_MILLIS
import app.proyekakhir.core.util.Constants.ANIMATION_SLOW_MILLIS
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentRegionCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RegionCameraFragment : Fragment() {

    private var _binding: FragmentRegionCameraBinding? = null
    private val binding get() = _binding!!

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private lateinit var viewFinderRect: Rect
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private val cameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { grant ->
            var isGranted = true
            if (!grant) isGranted = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when {
                    isGranted -> {
                        connectCamera()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {

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

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK


    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegionCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun sendImage(croppedBitmap: Bitmap) {
        lifecycleScope.launch(Default) {
            val path = tempFileImage(croppedBitmap, UUID.randomUUID().toString())
            lifecycleScope.launch(Main) {
                Log.i("TAG", "sendImage: $path")
                if (path != null) {
                    findNavController().navigate(R.id.action_regionCameraFragment_to_signUpFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()

        cameraExecutor.shutdown()
    }

    private fun connectCamera() {

        binding.viewFinder.post {
            binding.cameraCaptureButton.setOnClickListener {
                // Get a stable reference of the modifiable image capture use case
                binding.cameraCaptureButton.hide()
                imageCapture?.let { imageCapture ->

                    val file =
                        File(requireContext().cacheDir, UUID.randomUUID().toString() + ".jpg")
                    // Create output options object which contains file + metadata
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                    lifecycleScope.launch(IO) {
                        imageCapture.takePicture(
                            outputOptions,
                            cameraExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onError(exc: ImageCaptureException) {
                                    Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
                                }

                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    Log.i("TAG", "Photo capture succeeded")
                                    val savedUri = output.savedUri ?: Uri.fromFile(file)

                                    val bytes = file.readBytes()
                                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    val cropped = cropImage(
                                        rotateBitmap(savedUri.path!!, bmp),
                                        Size(binding.viewFinder.width, binding.viewFinder.height),
                                        viewFinderRect
                                    )
                                    sendImage(cropped)

                                }
                            })
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        // Display flash animation to indicate that photo was captured
                        binding.root.postDelayed({
                            binding.root.foreground = ColorDrawable(Color.WHITE)
                            binding.root.postDelayed(
                                { binding.root.foreground = null }, ANIMATION_FAST_MILLIS
                            )
                        }, ANIMATION_SLOW_MILLIS)
                    }

                }
            }


            cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            viewFinderRect = Rect(
                binding.borderCamera.left,
                binding.borderCamera.top,
                binding.borderCamera.right,
                binding.borderCamera.bottom
            )
            binding.viewFinderBackground.setViewFinderRect(viewFinderRect)

            cameraProviderFuture?.addListener({
                cameraProvider = cameraProviderFuture?.get()
                if (cameraProvider != null) cameraProvider?.unbindAll()
                try {

                    val metrics =
                        DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }

                    val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

                    // CameraProvider
                    val cameraProvider = cameraProvider
                        ?: throw IllegalStateException("Camera initialization failed.")

                    val cameraSelector =
                        CameraSelector.Builder().requireLensFacing(lensFacing).build()
                    preview = Preview.Builder().apply {
                        setTargetAspectRatio(screenAspectRatio)
                        setTargetRotation(binding.viewFinder.display.rotation)
                    }.build()

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetAspectRatio(screenAspectRatio)
                        .setTargetRotation(binding.viewFinder.display.rotation)
                        .build()
                    imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetAspectRatio(screenAspectRatio)
                        .setTargetRotation(binding.viewFinder.display.rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                        .setImageQueueDepth(10)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, { imageProxy ->
                                imageProxy.close()
                            })
                        }

                    try {
                        camera = cameraProvider.bindToLifecycle(
                            this, cameraSelector, preview, imageCapture, imageAnalyzer
                        )
                        setupTapForFocus(camera?.cameraControl!!)
                        preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    } catch (exc: Exception) {
                        Log.e("TAG", "Use case binding failed", exc)
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }


            }, ContextCompat.getMainExecutor(requireContext()))


        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTapForFocus(cameraControl: CameraControl) {
        binding.viewFinder.setOnTouchListener { _, event ->

            return@setOnTouchListener when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                        binding.viewFinder.width.toFloat(), binding.viewFinder.height.toFloat()
                    )
                    val autoFocusPoint = factory.createPoint(event.x, event.y)
                    try {
                        cameraControl.startFocusAndMetering(
                            FocusMeteringAction.Builder(
                                autoFocusPoint,
                                FocusMeteringAction.FLAG_AF
                            ).apply {
                                //focus only when the user tap the preview
                                disableAutoCancel()
                            }.build()
                        )
                    } catch (e: CameraInfoUnavailableException) {
                        Log.d("ERROR", "cannot access camera", e)
                    }
                    true
                }
                else -> false // Unhandled event.
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) setupPermission() else connectCamera()
    }

    private fun setupPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            )
        ) {
            showPermissionSnackBar(
                R.string.permission_rationale,
                android.R.string.ok
            ) { requestPermission() }
        } else requestPermission()
    }

    private fun requestPermission() {
        cameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED


}