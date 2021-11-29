package app.proyekakhir.driverapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentPhoneBinding
import java.lang.ref.WeakReference

class PhoneFragment : Fragment() {
    private var _binding: FragmentPhoneBinding? = null
    private val binding get() = _binding!!

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

            edtPhoneNumber.setOnEditorActionListener { _, id, _ ->
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_ACTION_NEXT) {
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
                            val action =
                                PhoneFragmentDirections.actionPhoneFragmentToOtpFragment(
                                    edtPhoneNumber.text.toString()
                                )
                            findNavController().navigate(action)
                        }
                    }
                }
                true
            }

            fabVerifikasi.setOnClickListener {
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
                        val action =
                            PhoneFragmentDirections.actionPhoneFragmentToOtpFragment(
                                edtPhoneNumber.text.toString()
                            )
                        findNavController().navigate(action)
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