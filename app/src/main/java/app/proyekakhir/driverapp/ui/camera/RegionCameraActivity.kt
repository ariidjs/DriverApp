package app.proyekakhir.driverapp.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.util.*
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.ActivityRegionCameraBinding
import app.proyekakhir.driverapp.databinding.FragmentRegionCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RegionCameraActivity : AppCompatActivity() {

    private var _binding: ActivityRegionCameraBinding? = null
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegionCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun sendImage(croppedBitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.IO) {
            val path = tempFileImage(croppedBitmap, UUID.randomUUID().toString())
            lifecycleScope.launch(Dispatchers.Main) {
                if (path != null) {
                    when (intent.getIntExtra("type", 0)) {

                        Constants.KEY_PATH_STNK -> {
                            val data = Intent()
                            data.putExtra("stnk", path)
                            setResult(Activity.RESULT_OK, data)
                            finish()
                        }
                        Constants.KEY_PATH_KTP -> {
                            val data = Intent()
                            data.putExtra("ktp", path)
                            setResult(Activity.RESULT_OK, data)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun connectCamera() {

        binding.viewFinder.post {
            binding.cameraCaptureButton.setOnClickListener {
                // Get a stable reference of the modifiable image capture use case

                binding.cameraCaptureButton.hide()
                imageCapture?.let { imageCapture ->

                    val file =
                        File(applicationContext.cacheDir, UUID.randomUUID().toString() + ".jpg")
                    // Create output options object which contains file + metadata
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                    lifecycleScope.launch(Dispatchers.IO) {
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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                        // Display flash animation to indicate that photo was captured
                        binding.root.postDelayed({
                            binding.root.foreground = ColorDrawable(Color.WHITE)
                            binding.root.postDelayed(
                                { binding.root.foreground = null }, Constants.ANIMATION_FAST_MILLIS
                            )
                        }, Constants.ANIMATION_SLOW_MILLIS)
                    }

                }
            }


            cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)
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


            }, ContextCompat.getMainExecutor(applicationContext))


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
                this,
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
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}