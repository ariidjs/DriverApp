package app.proyekakhir.driverapp.ui.auth

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.response.login.LoginResponse
import app.proyekakhir.core.data.source.remote.response.signup.SignUpResponse
import app.proyekakhir.core.domain.model.auth.LoginData
import app.proyekakhir.core.domain.model.auth.SignUpData
import app.proyekakhir.core.domain.usecase.MyUseCase
import kotlinx.coroutines.launch

class AuthViewModel(private val myUseCase: MyUseCase) : ViewModel() {
    val login = MediatorLiveData<Resource<LoginResponse>>()
    val signUp = MediatorLiveData<Resource<SignUpResponse>>()
    fun loginDriver(loginData: LoginData) {
        viewModelScope.launch {
            login.addSource(myUseCase.loginDriver(loginData).asLiveData()) {
                login.value = it
            }
        }
    }

    fun signUpDriver(signUpData: SignUpData) {
        viewModelScope.launch {
            signUp.addSource(myUseCase.signUpDriver(signUpData)) {
                signUp.value = it
            }
        }
    }
}