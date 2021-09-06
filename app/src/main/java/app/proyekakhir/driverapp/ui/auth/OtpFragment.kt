package app.proyekakhir.driverapp.ui.auth

import android.os.Bundle
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.util.Constants.TIMER_VALUE
import app.proyekakhir.core.util.Constants.TYPE_REGISTER
import app.proyekakhir.core.util.hide
import app.proyekakhir.core.util.show
import app.proyekakhir.core.util.showSoftKeyboard
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentOtpBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.shashank.sony.fancytoastlib.FancyToast
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class OtpFragment : Fragment() {
    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private val firebaseAuth: FirebaseAuth by inject()
    private var timer: CountDownTimer? = null
    private var verificationId: String? = null
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
            val phoneNumber =
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
                otpView.requestFocus()
                showSoftKeyboard()
                otpView.setOtpCompletionListener {
                    val credential = PhoneAuthProvider.getCredential(verificationId!!, it)
                    signInWithPhoneAuthCredential(credential)
                }
                btnChangeNumber.setOnClickListener {
                    val action = OtpFragmentDirections.actionOtpFragmentToPhoneFragment(
                        TYPE_REGISTER
                    )
                    findNavController().navigate(action)
                }
                btnKirimUlang.setOnClickListener {
                    if (phoneNumber.isNotEmpty()) {
                        startTimer()
                        resendVerificationCode(phoneNumber, resendToken)
                    }
                }
            }
        }
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
                    showToast("Success", FancyToast.SUCCESS)
                } else {
                    binding.otpView.text = null
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showToast("Kode OTP Salah!", FancyToast.ERROR)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error!", Toast.LENGTH_LONG).show()
            }
    }

    private fun verificationCallback() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                binding.otpView.setText(credential.smsCode)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.btnChangeNumber.show()
                timer?.cancel()
                binding.otpView.text = null
                binding.otpView.isEnabled = false
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