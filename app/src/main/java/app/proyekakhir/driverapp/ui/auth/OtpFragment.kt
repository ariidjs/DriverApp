package app.proyekakhir.driverapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.domain.model.auth.LoginData
import app.proyekakhir.core.ui.BaseFragment
import app.proyekakhir.core.util.*
import app.proyekakhir.core.util.Constants.KEY_API_TOKEN
import app.proyekakhir.core.util.Constants.KEY_FCM_TOKEN
import app.proyekakhir.core.util.Constants.KEY_ID_DRIVER
import app.proyekakhir.core.util.Constants.KEY_PHONE
import app.proyekakhir.core.util.Constants.TIMER_VALUE
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentOtpBinding
import app.proyekakhir.driverapp.ui.home.HomeActivity
import app.proyekakhir.driverapp.util.handleAuth
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging
import com.shashank.sony.fancytoastlib.FancyToast
import id.ionbit.ionalert.IonAlert
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit


class OtpFragment : BaseFragment() {
    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private val firebaseAuth: FirebaseAuth by inject()
    private var timer: CountDownTimer? = null
    private var verificationId: String? = null
    private val authViewModel: AuthViewModel by viewModel()
    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.slide_right)
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verificationCallback()
        arguments?.let {
            phoneNumber =
                StringBuilder().append("+62").append(OtpFragmentArgs.fromBundle(it).nohp).toString()
            if (phoneNumber.isNotEmpty()) {
                startTimer()
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,
                    5,
                    TimeUnit.SECONDS,
                    requireActivity(),
                    callbacks!!
                )
            }

            with(binding) {
                txtPhone.text = getString(R.string.otp_desc, phoneNumber)
                tvOtpPin.requestFocus()
                showSoftKeyboard()
                tvOtpPin.setOtpCompletionListener { code ->
                    hideSoftKeyboard()
                    val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                    signInWithPhoneAuthCredential(credential)
                }
                btnChangeNumber.setOnClickListener {
                    findNavController().navigate(R.id.action_otpFragment_to_phoneFragment)
                }
                btnKirimUlang.setOnClickListener {
                    if (phoneNumber.isNotEmpty()) {
                        startTimer()
                        resendVerificationCode(phoneNumber, resendToken)
                    }
                }
            }

            authViewModel.login.observe(viewLifecycleOwner, { response ->
                when (response) {
                    is Resource.Success -> {
                        lifecycleScope.launch {
                            if (response.value.message.startsWith("phone")) {
                                findNavController().navigate(R.id.action_otpFragment_to_signUpFragment)
                            } else {
                                localProperties.saveApiToken(KEY_API_TOKEN, response.value.jwt)
                                localProperties.saveIdDriver(KEY_ID_DRIVER, response.value.data.id)
                                localProperties.saveFcm(KEY_FCM_TOKEN, response.value.data.fcm)
                                startActivity(Intent(requireContext(), HomeActivity::class.java))
                                requireActivity().finish()
                            }
                        }


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


    }

    private fun observableData() {

    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(5L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks!!)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    timer?.cancel()
                    checkPhone()
                } else {
                    binding.tvOtpPin.text = null
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showToast("Kode OTP Salah!", FancyToast.ERROR)
                    }
                }
            }.addOnFailureListener { e ->
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        showToast("Kode OTP Salah!", FancyToast.ERROR)
                    }
                    is FirebaseTooManyRequestsException -> {
                        // The SMS quota for the project has been exceeded
                        showToast(
                            "The SMS quota for the project has been exceeded",
                            FancyToast.ERROR
                        )
                    }
                    else -> {
                        showToast("Gagal verifikasi OTP! ${e.message}", FancyToast.ERROR)
                    }
                }
            }
    }

    private fun checkPhone() {
        //getting fcm token
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val noHp = phoneNumber.replace("+62", "")
                localProperties.savePhone(KEY_PHONE, "0$noHp")
                authViewModel.loginDriver(LoginData(token, "0$noHp"))
            }
            .addOnFailureListener {
                showToast("Login Error! Please try again later!", FancyToast.ERROR)
            }

    }

    private fun verificationCallback() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                binding.tvOtpPin.setText(credential.smsCode)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.btnChangeNumber.show()
                timer?.cancel()
                binding.tvOtpPin.text = null
                binding.tvOtpPin.isEnabled = false
                showToast("Gagal mengirim kode OTP", FancyToast.ERROR)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    showToast("No HP yang anda masukkan salah!", FancyToast.ERROR)
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    showToast("The SMS quota for the project has been exceeded", FancyToast.ERROR)
                }
            }

            override fun onCodeSent(
                id: String,
                phoneAuthProvider: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(id, phoneAuthProvider)
                verificationId = id
                resendToken = phoneAuthProvider
                binding.btnKirimUlang.hide()
                showToast("Kode Terkirim", FancyToast.SUCCESS)
            }

        }
    }

    private fun startTimer() {
        binding.txtTimer.show()
        timer = object : CountDownTimer(TIMER_VALUE, 1000) {
            override fun onTick(untilFinished: Long) {
                binding.txtTimer.text = DateUtils.formatElapsedTime(untilFinished / 1000)
            }

            override fun onFinish() {
                binding.btnKirimUlang.show()
                binding.txtTimer.hide()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        callbacks = null
        timer = null
    }
}