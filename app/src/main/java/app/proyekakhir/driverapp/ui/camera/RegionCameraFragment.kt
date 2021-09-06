package app.proyekakhir.driverapp.ui.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.proyekakhir.driverapp.databinding.FragmentRegionCameraBinding

class RegionCameraFragment : Fragment() {

    private var _binding: FragmentRegionCameraBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegionCameraBinding.inflate(inflater, container, false)
        return binding.root
    }
}