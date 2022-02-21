package app.proyekakhir.driverapp.ui.home.ui.balance

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.domain.model.balance.DepositInput
import app.proyekakhir.core.ui.BaseDialogFragment
import app.proyekakhir.core.util.*
import app.proyekakhir.core.util.Constants.TYPE_DEPOSIT
import app.proyekakhir.core.util.Constants.TYPE_WITHDRAW
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentDepositBinding
import app.proyekakhir.driverapp.util.handleResponses
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.iceteck.silicompressorr.FileUtils
import com.shashank.sony.fancytoastlib.FancyToast
import id.ionbit.ionalert.IonAlert
import org.koin.android.ext.android.inject
import java.util.*


class DepositFragment : BaseDialogFragment(), AdapterView.OnItemClickListener {

    private var _binding: FragmentDepositBinding? = null
    private val binding get() = _binding!!
    private val balanceViewModel: BalanceViewModel by inject()
    private var namaBank: String = ""
    private var image: String? = null
    private val camera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                image = tempFileImage(result.data?.extras?.get("data") as Bitmap, UUID.randomUUID().toString())
            }
        }
    private val gallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                image = FileUtils.getPath(requireContext(), result.data?.data)
            }
        }

    private val cameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { grant ->
            var isGranted = true
            if (!grant) isGranted = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    isGranted -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        camera.launch(intent)
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: BottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        _binding = FragmentDepositBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(binding.root)

        loadingDialog = IonAlert(requireContext(), IonAlert.PROGRESS_TYPE)
            .setSpinKit("Circle")
            .setSpinColor("#FFBD4A")
        balanceViewModel.deposit.observe(requireActivity(), { response ->
            when (response) {
                is Resource.Success -> {
                    showToast("Success", FancyToast.SUCCESS)
                    dismiss()
                }
                is Resource.Error -> {
                    handleResponses(response)
                }
                is Resource.Loading -> {
                    when (response.isLoading) {
                        true -> showLoading()
                        false -> hideLoading()
                    }
                }
            }
        })

        binding.edtNamaBank.apply {
            setAdapter(
                ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_dropdown_item,
                    resources.getStringArray(R.array.bank)
                )
            )
            threshold = 1
            onItemClickListener = this@DepositFragment
        }
        arguments?.let {
            val type = DepositFragmentArgs.fromBundle(it).type
            if (type == TYPE_DEPOSIT) {
                with(binding) {
                    cardImage.show()
                    tvTitle.show()
                    cardImage.setOnClickListener {
                        showPicker()
                    }
                    btnProses.setOnClickListener {
                        if (TextUtils.isEmpty(edtNoRek.text.toString()) && TextUtils.isEmpty(
                                edtNominal.text.toString()
                            ) && TextUtils.isEmpty(edtNama.text.toString())
                        ) {
                            edtNama.error = getString(R.string.input_error)
                            edtNominal.error = getString(R.string.input_error)
                            edtNoRek.error = getString(R.string.input_error)
                            showToast("Silahkan di cek kembali datanya", FancyToast.ERROR)
                        } else {
                            balanceViewModel.depositOrWithDraw(
                                DepositInput(
                                    edtNama.text.toString(),
                                    edtNoRek.text.toString(),
                                    edtNominal.text.toString(),
                                    namaBank,
                                    TYPE_DEPOSIT.toString(),
                                    Uri.parse(image)
                                )
                            )
                        }
                    }
                }

            } else {
                with(binding) {
                    cardImage.hide()
                    tvTitle.hide()
                    btnProses.setOnClickListener {
                        if (TextUtils.isEmpty(edtNoRek.text.toString()) && TextUtils.isEmpty(
                                edtNominal.text.toString()
                            ) && TextUtils.isEmpty(edtNama.text.toString())
                        ) {
                            edtNama.error = getString(R.string.input_error)
                            edtNominal.error = getString(R.string.input_error)
                            edtNoRek.error = getString(R.string.input_error)
                            showToast("Silahkan di cek kembali datanya", FancyToast.LENGTH_SHORT)
                        } else {
                            balanceViewModel.depositOrWithDraw(
                                DepositInput(
                                    edtNama.text.toString(),
                                    edtNoRek.text.toString(),
                                    edtNominal.text.toString(),
                                    namaBank,
                                    TYPE_WITHDRAW.toString(),
                                    null
                                )
                            )
                        }
                    }
                }


            }
        }

        return dialog
    }

    private fun showPicker() {
        val options = arrayOf<CharSequence>("Ambil dari kamera", "Pilih di gallery")

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Choose your picture")
        builder.setCancelable(true)

        builder.setItems(options) { _, item ->
            when (item) {
                0 -> {
                    if (!checkPermissions()) setupPermission() else {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        camera.launch(intent)
                    }
                }
                1 -> {
                    val pickPhoto =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    gallery.launch(pickPhoto)
                }
            }
        }
        builder.show()
    }

    override fun onItemClick(adapterView: AdapterView<*>?, parent: View?, pos: Int, p3: Long) {
        namaBank = adapterView?.getItemAtPosition(pos).toString()
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