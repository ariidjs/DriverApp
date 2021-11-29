package app.proyekakhir.driverapp.ui.home.ui.transaction

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.domain.model.transaction.MessageData
import app.proyekakhir.core.util.convertToIDR
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.databinding.FragmentIncomingBinding
import app.proyekakhir.driverapp.util.disableDoubleClick
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shashank.sony.fancytoastlib.FancyToast
import id.ionbit.ionalert.IonAlert
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject

class IncomingFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentIncomingBinding? = null
    private val binding get() = _binding!!
    private lateinit var countDownTimer: CountDownTimer
    private val viewModel: TransactionViewModel by inject()
    private lateinit var loadingDialog: IonAlert
    private var isPaused = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: BottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        _binding = FragmentIncomingBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(binding.root)
        showTimer()
        loadingDialog = IonAlert(requireContext(), IonAlert.PROGRESS_TYPE)
            .setSpinKit("Circle")
            .setSpinColor("#FFBD4A")
        val mBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBehavior.isDraggable = false
        mBehavior.isHideable = false

        arguments?.let {
            val data = IncomingFragmentArgs.fromBundle(it).transactionMessage
            setData(data)
            binding.btnCancel.setOnClickListener { dismiss() }
            binding.btnAccept.setOnClickListener { button ->
                button.disableDoubleClick()
                isPaused = true
                viewModel.acceptOrder(data.idTrans)
            }
            viewModel.accept.observe(requireActivity(), { response ->
                when (response) {
                    is Resource.Success -> {
                        showToast("SUCCESS", FancyToast.SUCCESS)

                        EventBus.getDefault().postSticky(response.value)
                        dismiss()
                    }
                    is Resource.Error -> {
                        showToast(
                            "ERROR ${response.errorBody?.string()}, ${response.errorCode}",
                            FancyToast.ERROR
                        )
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

    private fun showTimer() {
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
                dismiss()
//                if (!isPaused) decisionOrder(noTrans, false)
            }

        }.start()
    }

}