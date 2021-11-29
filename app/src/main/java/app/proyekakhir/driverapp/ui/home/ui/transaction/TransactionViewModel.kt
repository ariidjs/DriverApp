package app.proyekakhir.driverapp.ui.home.ui.transaction

import androidx.lifecycle.*
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.response.transaction.HistoryResponse
import app.proyekakhir.core.data.source.remote.response.transaction.TransactionResponse
import app.proyekakhir.core.domain.usecase.MyUseCase
import kotlinx.coroutines.launch

class TransactionViewModel(private val myUseCase: MyUseCase) : ViewModel() {

    val accept = MediatorLiveData<Resource<TransactionResponse>>()

    fun acceptOrder(idTrans: Int) {
        viewModelScope.launch {
            accept.addSource(myUseCase.acceptOrder(idTrans).asLiveData()) {
                accept.value = it
            }
        }
    }

    val finish = MediatorLiveData<Resource<TransactionResponse>>()

    fun finishOrder(idTrans: Int) {
        viewModelScope.launch {
            finish.addSource(myUseCase.finishOrder(idTrans).asLiveData()) {
                finish.value = it
            }
        }
    }

    val current = MediatorLiveData<Resource<TransactionResponse>>()

    fun getCurrentOrder() {
        viewModelScope.launch {
            current.addSource(myUseCase.getCurrentOrder().asLiveData()) {
                current.value = it
            }
        }
    }

    val history = MediatorLiveData<Resource<HistoryResponse>>()

    fun getHistory() {
        viewModelScope.launch {
            history.addSource(myUseCase.getHistory().asLiveData()) {
                history.value = it
            }
        }
    }

}