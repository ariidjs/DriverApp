package app.proyekakhir.driverapp.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.ui.BaseDialogFragment
import app.proyekakhir.core.util.Constants.ERROR_INSUFFICIENT_BALANCE
import app.proyekakhir.core.util.Constants.ERROR_NOT_REGISTER
import app.proyekakhir.core.util.Constants.ERROR_UNAUTHORIZED
import app.proyekakhir.driverapp.R
import app.proyekakhir.driverapp.databinding.FragmentDialogBinding
import app.proyekakhir.driverapp.ui.auth.MainActivity


class DialogFragment : BaseDialogFragment() {
    private var _binding: FragmentDialogBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabClose.setOnClickListener {
            dismiss()
        }
        arguments?.let {
            when(DialogFragmentArgs.fromBundle(it).type) {
                ERROR_UNAUTHORIZED -> {
                    binding.fabClose.hide()
                    binding.tvTitle.text = getString(R.string.title_unauth)
                    binding.tvDesc.text = getString(R.string.desc_unauth)
                    binding.imgIlustration.setImageResource(R.drawable.ic_maintanance)
                    binding.btnOk.text = getString(R.string.btn_unauth)
                    binding.btnOk.setOnClickListener {
                        localProperties.clearSession()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                        dismiss()
                    }
                }
                ERROR_NOT_REGISTER -> {
                    binding.fabClose.hide()
                    binding.tvTitle.text = getString(R.string.title_not_registered)
                    binding.tvDesc.text = getString(R.string.desc_not_registered)
                    binding.imgIlustration.setImageResource(R.drawable.img_join)
                    binding.btnOk.text = getString(R.string.daftar_sini)
                    binding.btnOk.setOnClickListener {
                        findNavController().navigate(R.id.action_dialogFragment3_to_signUpFragment)
                        dismiss()
                    }
                }
                ERROR_INSUFFICIENT_BALANCE -> {
                    binding.fabClose.hide()
                    binding.tvTitle.text = getString(R.string.insufficient_title)
                    binding.tvDesc.text = getString(R.string.desc_insufficient)
                    binding.imgIlustration.setImageResource(R.drawable.img_insufficient)
                    binding.btnOk.text = getString(R.string.btn_saldo_direct)
                    binding.btnOk.setOnClickListener {
                        findNavController().navigate(R.id.action_dialogFragment_to_balanceFragment)
                        dismiss()
                    }
                }
            }
        }
    }

    override fun getTheme() = R.style.CustomBottomSheetDialogTheme
}