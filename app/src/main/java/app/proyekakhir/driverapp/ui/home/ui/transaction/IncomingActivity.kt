package app.proyekakhir.driverapp.ui.home.ui.transaction

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.domain.model.transaction.MessageData
import app.proyekakhir.core.ui.BaseActivity
import app.proyekakhir.core.util.Constants
import app.proyekakhir.core.util.convertToIDR
import app.proyekakhir.core.util.showToast
import app.proyekakhir.driverapp.AlarmReceiver
import app.proyekakhir.driverapp.databinding.ActivityIncomingBinding
import app.proyekakhir.driverapp.ui.home.HomeActivity
import app.proyekakhir.driverapp.util.disableDoubleClick
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shashank.sony.fancytoastlib.FancyToast
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject

class IncomingActivity : BaseActivity() {
    private var _binding: ActivityIncomingBinding? = null
    private val binding get() = _binding!!
    private lateinit var countDownTimer: CountDownTimer
    private val viewModel: TransactionViewModel by inject()
    private val firebaseDatabase: FirebaseDatabase by inject()
    private lateinit var driverRef: DatabaseReference
    private var isPaused = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityIncomingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        driverRef =
            firebaseDatabase.getReference(Constants.DRIVER_DATA_REFERENCE)
                .child(localProperties.idDriver.toString())
        intent.getParcelableExtra<MessageData>("orders").let { data ->
            if (data != null) {
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
            }
        }

        viewModel.accept.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    EventBus.getDefault().postSticky(response.value)
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is Resource.Error -> {
                    finish()
                }
                is Resource.Loading -> {
                    when (response.isLoading) {
                        true -> loadingDialog.show()
                        false -> loadingDialog.hide()
                    }
                }
            }
        })
        viewModel.decline.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is Resource.Error -> {
                    showToast(
                        "ERROR ${response.errorBody?.string()}, ${response.errorCode}",
                        FancyToast.ERROR
                    )
                    finish()
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

    private fun setBanTemp() {
        driverRef.child("status").setValue(3)
        AlarmReceiver().setBannedTime(this)
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
}