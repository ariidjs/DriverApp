package app.proyekakhir.driverapp.ui.camera

import android.Manifest
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.proyekakhir.core.util.*
import app.proyekakhir.core.util.Constants.ANIMATION_FAST_MILLIS
import app.proyekakhir.core.util.Constants.ANIMATION_SLOW_MILLIS
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentRegionCameraBinding
import com.google.common.util.concurrent.ListenableFuture
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
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.cameraCaptureButton.setOnClickListener {
            // Get a stable reference of the modifiable image capture use case
            imageCapture?.let { imageCapture ->

                val file = File(requireContext().cacheDir, UUID.randomUUID().toString() + ".jpg")

                // Create output options object which contains file + metadata
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = output.savedUri ?: Uri.fromFile(file)

                            val bitmap = BitmapFactory.decodeFile(file.path)

                            val cropped = cropImage(
                                rotateBitmap(savedUri.path!!, bitmap),
                                Size(binding.viewFinder.width, binding.viewFinder.height),
                                viewFinderRect
                            )
                            sendImage(cropped)
                            Log.i("TAG", "Photo capture succeeded")
                        }
                    })
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
    }

    private fun sendImage(croppedBitmap: Bitmap) {
        val path = tempFileImage(requireContext(), croppedBitmap, UUID.randomUUID().toString())
        Log.i("TAG", "sendImage: $path")
        //        Toast.makeText(getActivity(), "Saved "+file, Toast.LENGTH_SHORT).show();
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()

        cameraExecutor.shutdown()
    }

    private fun connectCamera() {

        binding.viewFinder.post {
            viewFinderRect = Rect(
                binding.borderCamera.left,
                binding.borderCamera.top,
                binding.borderCamera.right,
                binding.borderCamera.bottom
            )
            binding.viewFinderBackground.setViewFinderRect(viewFinderRect)

            cameraProviderFuture?.addListener({
                if (cameraProvider != null) cameraProvider?.unbindAll()
                try {
                    cameraProvider = cameraProviderFuture?.get()
                    val metrics =
                        DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }

                    val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

                    val cameraSelector =
                        CameraSelector.Builder().requireLensFacing(lensFacing).build()
                    preview = Preview.Builder().apply {
                        setTargetAspectRatio(screenAspectRatio)
                        setTargetRotation(binding.viewFinder.display.rotation)
                    }.build()

                    imageCapture = ImageCapture.Builder().apply {
                        setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        setTargetRotation(binding.viewFinder.display.rotation)
                        setTargetAspectRatio(screenAspectRatio)
                    }.build()

                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                    preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(requireContext()))
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