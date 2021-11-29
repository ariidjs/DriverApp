package app.proyekakhir.driverapp.ui.camera

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.proyekakhir.core.util.Constants.KEY_PATH_KTP
import app.proyekakhir.core.util.Constants.KEY_PATH_STNK
import app.proyekakhir.driverapp.databinding.FragmentPhotoBinding
import com.bumptech.glide.Glide

class PhotoFragment : Fragment() {

    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener { activity?.onBackPressed() }
        arguments?.let {
            when (PhotoFragmentArgs.fromBundle(it).type) {
                KEY_PATH_KTP -> {
                    val pathKtp = PhotoFragmentArgs.fromBundle(it).path
                    Glide.with(view).load(pathKtp).into(binding.image)

                    binding.btnSave.setOnClickListener {
                        val action = PhotoFragmentDirections.actionPhotoFragmentToSignUpFragment(
                            KEY_PATH_KTP,
                            pathKtp
                        )
                        findNavController().navigate(action)
                    }
                }
                KEY_PATH_STNK -> {
                    val pathStnk = PhotoFragmentArgs.fromBundle(it).path
                    Glide.with(view).load(pathStnk).into(binding.image)

                    binding.btnSave.setOnClickListener {
                        val action = PhotoFragmentDirections.actionPhotoFragmentToSignUpFragment(
                            KEY_PATH_STNK,
                            pathStnk
                        )
                        findNavController().navigate(action)
                    }
                }
                else -> Log.i("TAG", "onViewCreated: ")
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}