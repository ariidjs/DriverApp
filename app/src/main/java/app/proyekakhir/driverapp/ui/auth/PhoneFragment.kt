package app.proyekakhir.driverapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.util.Constants.TYPE_FORGOT_PASSWORD
import app.proyekakhir.core.util.Constants.TYPE_REGISTER
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentPhoneBinding

class PhoneFragment : Fragment() {
    private var _binding: FragmentPhoneBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_phoneFragment_to_loginFragment)
        }
        with(binding) {
            fabBackPhone.setOnClickListener {
                activity?.onBackPressed()
            }
            arguments?.let {
                val type = PhoneFragmentArgs.fromBundle(it).type
                btnVerifikasi.setOnClickListener {
                    when {
                        edtPhoneNumber.text.isNullOrEmpty() -> {
                            edtPhoneNumber.error = getString(R.string.input_error)
                            edtPhoneNumber.requestFocus()
                        }
                        edtPhoneNumber.text.toString().startsWith("0") -> {
                            edtPhoneNumber.error = "Silahkan hapus angka 0"
                            edtPhoneNumber.requestFocus()
                        }
                        else -> {
                            if (type == TYPE_REGISTER) {
                                val action =
                                    PhoneFragmentDirections.actionPhoneFragmentToOtpFragment(
                                        TYPE_REGISTER, edtPhoneNumber.text.toString()
                                    )
                                findNavController().navigate(action)
                            } else {
                                val action =
                                    PhoneFragmentDirections.actionPhoneFragmentToOtpFragment(
                                        TYPE_FORGOT_PASSWORD, edtPhoneNumber.text.toString()
                                    )
                                findNavController().navigate(action)
                            }
                        }
                    }
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}