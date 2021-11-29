package app.proyekakhir.driverapp.ui.home.ui.balance

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.response.balance.BalanceResponse
import app.proyekakhir.core.data.source.remote.response.balance.DepositResponse
import app.proyekakhir.core.domain.model.balance.DepositInput
import app.proyekakhir.core.domain.usecase.MyUseCase
import kotlinx.coroutines.launch

class BalanceViewModel(private val myUseCase: MyUseCase) : ViewModel() {

    val balance = MediatorLiveData<Resource<BalanceResponse>>()

    fun getHistoryBalance() {
        viewModelScope.launch {
            balance.addSource(myUseCase.getHistoryBalance().asLiveData()) {
                balance.value = it
            }
        }
    }

    val deposit = MediatorLiveData<Resource<DepositResponse>>()

    fun depositOrWithDraw(depositInput: DepositInput) {
        viewModelScope.launch {
            deposit.addSource(myUseCase.depositOrWithDraw(depositInput).asLiveData()) {
                deposit.value = it
            }
        }
    }
}