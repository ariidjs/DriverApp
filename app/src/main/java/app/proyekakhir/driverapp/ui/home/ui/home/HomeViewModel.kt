package app.proyekakhir.driverapp.ui.home.ui.home

import android.location.Location
import androidx.lifecycle.*
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.LocationListener
import app.proyekakhir.core.data.source.remote.response.account.AccountResponse
import app.proyekakhir.core.data.source.remote.response.direction.DirectionResponse
import app.proyekakhir.core.data.source.remote.response.transaction.TransactionResponse
import app.proyekakhir.core.domain.model.direction.DirectionData
import app.proyekakhir.core.domain.usecase.MyUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val locationRepository: LocationListener,
    private val myUseCase: MyUseCase
) : ViewModel() {
    val location: LiveData<Location> get() = locationRepository.location
    fun requestLocation() {
        locationRepository.updateLocation(true)
    }

    fun removeLocation() = locationRepository.removeLocationUpdates()


    val accounts = MediatorLiveData<Resource<AccountResponse>>()
    fun getAccount() {
        viewModelScope.launch {
            accounts.addSource(myUseCase.getAccount().asLiveData()) {
                accounts.value = it
            }
        }
    }

    val validation = MediatorLiveData<Resource<TransactionResponse>>()
    fun validationCode(idTrans: Int, code: String) {
        viewModelScope.launch {
            validation.addSource(
                myUseCase.validationCodeToStore(idTrans, code).asLiveData()
            ) {
                validation.value = it
            }
        }
    }

    val directionStore = MediatorLiveData<Resource<DirectionResponse>>()

    fun directionStore(directionData: DirectionData) {
        viewModelScope.launch {
            directionStore.addSource(myUseCase.getDirectionStore(directionData).asLiveData()) {
                directionStore.value = it
            }
        }
    }

    val directionUser = MediatorLiveData<Resource<DirectionResponse>>()

    fun directionUser(directionData: DirectionData) {
        viewModelScope.launch {
            directionUser.addSource(myUseCase.getDirectionCustomer(directionData).asLiveData()) {
                directionUser.value = it
            }
        }
    }
}