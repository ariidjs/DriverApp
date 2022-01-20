package app.proyekakhir.driverapp.ui.auth

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.domain.model.auth.SignUpData
import app.proyekakhir.core.ui.BaseFragment
import app.proyekakhir.core.util.Constants.KEY_PATH_KTP
import app.proyekakhir.core.util.Constants.KEY_PATH_STNK
import app.proyekakhir.core.util.Constants.pathFormal
import app.proyekakhir.core.util.Constants.pathKtp
import app.proyekakhir.core.util.Constants.pathStnk
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentSignUpBinding
import app.proyekakhir.driverapp.ui.camera.RegionCameraActivity
import app.proyekakhir.driverapp.util.checkInputSignUp
import com.iceteck.silicompressorr.FileUtils
import com.shashank.sony.fancytoastlib.FancyToast
import com.techiness.progressdialoglibrary.ProgressDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class SignUpFragment : BaseFragment(), AdapterView.OnItemClickListener {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModel()
    private var jeniskelamin: String? = null
    private lateinit var progressDialog: ProgressDialog
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                pathFormal = FileUtils.getPath(requireContext(), result.data?.data)
            }
        }

    private val getStnk =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                pathStnk = result.data?.getStringExtra("stnk")!!
            }
        }

    private val getKtp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                pathKtp = result.data?.getStringExtra("ktp")!!
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.apply {
            theme = ProgressDialog.THEME_LIGHT
            mode = ProgressDialog.MODE_DETERMINATE
            setTitle("Uploading files")
            hideNegativeButton()
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) { activity?.finish() }

        with(binding) {
            btnAddKtp.setOnClickListener {
                val intent = Intent(requireContext(), RegionCameraActivity::class.java)
                intent.putExtra("type", KEY_PATH_KTP)
                getKtp.launch(intent)
            }
            btnAddStnk.setOnClickListener {
                val intent = Intent(requireContext(), RegionCameraActivity::class.java)
                intent.putExtra("type", KEY_PATH_STNK)
                getStnk.launch(intent)
            }

            btnAddFormal.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                getContent.launch(intent)
            }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                resources.getStringArray(R.array.jk)
            )
            edtJk.setAdapter(adapter)
            edtJk.onItemClickListener = this@SignUpFragment
            btnDaftar.setOnClickListener { signUpData() }
            observableData()
        }

    }

    private fun observableData() {
        authViewModel.signUp.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    showToast(
                        "Sukses register! Silahkan login untuk mengetahui status akun anda!",
                        FancyToast.SUCCESS
                    )
                    progressDialog.dismiss()
                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                }
                is Resource.Error -> {
                    showToast(
                        "Register tidak berhasil! Silahkan coba beberapa saat lagi!",
                        FancyToast.ERROR
                    )
                    progressDialog.dismiss()
                }
                is Resource.Loading -> {
                    progressDialog.progress = response.progress!!
                    progressDialog.setMessage(response.message)
                }
            }
        })
    }

    private fun signUpData() {
        with(binding) {

//            authViewModel.signUpDriver(
//                SignUpData(
//                    "Ari",
//                    "ari@gmail.com",
//                    "1",
//                    "081265411456",
//                    "BA 1111 AE",
//                    "13700058481284597",
//                    "RTJR5554514",
//                    Uri.parse(pathFormal),
//                    Uri.parse(pathKtp),
//                    Uri.parse(pathStnk)
//                )
//            )
            if (checkInputSignUp(binding)) {
                progressDialog.show()
                authViewModel.signUpDriver(
                    SignUpData(
                        edtName.text.toString(),
                        edtEmail.text.toString(),
                        jeniskelamin!!,
                        localProperties.phone!!,
                        edtNoKendaraan.text.toString(),
                        edtNik.text.toString(),
                        edtNoStnk.text.toString(),
                        Uri.parse(pathFormal),
                        Uri.parse(pathKtp),
                        Uri.parse(pathStnk)

                    )
                )
            } else {
                edtEmail.requestFocus()
                edtNik.requestFocus()
                edtName.requestFocus()
                edtNoKendaraan.requestFocus()
                edtEmail.error = getString(R.string.input_error)
                edtNoStnk.error = getString(R.string.input_error)
                edtName.error = getString(R.string.input_error)
                edtNoKendaraan.error = getString(R.string.input_error)
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        jeniskelamin = position.toString()
    }

}

