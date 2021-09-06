package app.proyekakhir.driverapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.util.Constants.TYPE_REGISTER
import app.proyekakhir.driverapp.databinding.FragmentLoginBinding
import app.proyekakhir.driverapp.ui.home.HomeActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            activity?.finish()
        }
        with(binding) {
            btnDaftar.setOnClickListener {
                val action =
                    LoginFragmentDirections.actionLoginFragmentToPhoneFragment(TYPE_REGISTER)
                findNavController().navigate(action)
            }
            btnLogin.setOnClickListener {
                startActivity(
                    Intent(
                        requireContext(),
                        HomeActivity::class.java
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}