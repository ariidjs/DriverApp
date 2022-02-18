package app.proyekakhir.driverapp.ui.home.ui.transaction

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.local.LocalProperties
import app.proyekakhir.core.domain.model.transaction.MessageData
import app.proyekakhir.core.ui.BaseDialogFragment
import app.proyekakhir.core.util.Constants
import app.proyekakhir.core.util.convertToIDR
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.AlarmReceiver
import app.proyekakhir.driverapp.databinding.FragmentIncomingBinding
import app.proyekakhir.driverapp.util.disableDoubleClick
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shashank.sony.fancytoastlib.FancyToast
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject

class IncomingFragment : BaseDialogFragment() {

    private var _binding: FragmentIncomingBinding? = null
    private val binding get() = _binding!!

    private lateinit var countDownTimer: CountDownTimer
    private val viewModel: TransactionViewModel by inject()
    private var isPaused = false

    private val firebaseDatabase: FirebaseDatabase by inject()
    private lateinit var currentRef: DatabaseReference

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: BottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        _binding = FragmentIncomingBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(binding.root)
        val localProperties = LocalProperties(requireContext())
        currentRef = firebaseDatabase.getReference(Constants.DRIVER_REFERENCE)
                .child(localProperties.idDriver.toString())
        val mBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBehavior.isDraggable = false
        mBehavior.isHideable = false

        arguments?.let {
            val data = IncomingFragmentArgs.fromBundle(it).transactionMessage
            showTimer(data)
            setData(data)
            binding.btnCancel.setOnClickListener { button ->
                button.disableDoubleClick()
                isPaused = true
                viewModel.declineOrder(data.idTrans)
                setBanTemp()
            }
            binding.btnAccept.setOnClickListener { button ->
                button.disableDoubleClick()
                isPaused = true
                viewModel.acceptOrder(data.idTrans)
            }
            viewModel.accept.observe(requireActivity(), { response ->
                when (response) {
                    is Resource.Success -> {
                        EventBus.getDefault().postSticky(response.value)
                        dismiss()
                    }
                    is Resource.Error -> {
                        showToast(
                            "ERROR ${response.errorBody?.string()}, ${response.errorCode}",
                            FancyToast.ERROR
                        )
                        dismiss()
                    }
                    is Resource.Loading -> {
                        when (response.isLoading) {
                            true -> loadingDialog.show()
                            false -> loadingDialog.hide()
                        }
                    }
                }
            })
            viewModel.decline.observe(requireActivity(), { response ->
                when (response) {
                    is Resource.Success -> {
                        dismiss()
                    }
                    is Resource.Error -> {
                        showToast(
                            "ERROR ${response.errorBody?.string()}, ${response.errorCode}",
                            FancyToast.ERROR
                        )
                        dismiss()
                    }
                    is Resource.Loading -> {
                        when (response.isLoading) {
                            true -> loadingDialog.show()
                            false -> loadingDialog.hide()
                        }
                    }
                }
            })
        }
        return dialog
    }

    private fun setData(messageTrans: MessageData) {
        with(binding) {
            tvStoreAddress.text = messageTrans.storeAddress
            tvDriverPrice.text = convertToIDR(messageTrans.priceDriver.toInt())
            tvPrice.text = convertToIDR(messageTrans.totalPrice.toInt())
            tvStoreName.text = messageTrans.storeName

        }
    }

    private fun showTimer(data: MessageData) {
        var i = 0

        binding.progressbarOrder.progress = i
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isPaused) {
                    cancel()
                } else {
                    i++
                    binding.progressbarOrder.progress = i * 100 / (10000 / 1000)
                }

            }

            override fun onFinish() {
                i++
                binding.progressbarOrder.progress = 100
                viewModel.declineOrder(data.idTrans)
                setBanTemp()
            }

        }.start()
    }

    private fun setBanTemp() {
        currentRef.child("status").setValue(3)
        AlarmReceiver().setBannedTime(requireContext())
    }

}