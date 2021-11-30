package app.proyekakhir.core.domain.repository

import androidx.lifecycle.LiveData
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.response.account.AccountResponse
import app.proyekakhir.core.data.source.remote.response.balance.BalanceResponse
import app.proyekakhir.core.data.source.remote.response.balance.DepositResponse
import app.proyekakhir.core.data.source.remote.response.direction.DirectionResponse
import app.proyekakhir.core.data.source.remote.response.login.LoginResponse
import app.proyekakhir.core.data.source.remote.response.signup.SignUpResponse
import app.proyekakhir.core.data.source.remote.response.transaction.HistoryResponse
import app.proyekakhir.core.data.source.remote.response.transaction.TransactionResponse
import app.proyekakhir.core.domain.model.auth.Login
import app.proyekakhir.core.domain.model.auth.SignUp
import app.proyekakhir.core.domain.model.balance.Deposit
import app.proyekakhir.core.domain.model.direction.DirectionData
import app.proyekakhir.core.domain.model.direction.LatLngData
import app.proyekakhir.core.domain.model.transaction.AuthData
import kotlinx.coroutines.flow.Flow

interface IMyRepository {
    suspend fun login(login: Login): Flow<Resource<LoginResponse>>

    suspend fun signUp(signUp: SignUp): LiveData<Resource<SignUpResponse>>

    suspend fun getAccount(authData: AuthData): Flow<Resource<AccountResponse>>

    suspend fun acceptOrder(authData: AuthData, idTrans: Int): Flow<Resource<TransactionResponse>>

    suspend fun validationCodeStore(
        authData: AuthData,
        idTrans: Int,
        code: String
    ): Flow<Resource<TransactionResponse>>

    suspend fun finishOrder(authData: AuthData, idTrans: Int): Flow<Resource<TransactionResponse>>

    suspend fun getCurrentOrder(authData: AuthData): Flow<Resource<TransactionResponse>>

    suspend fun getHistory(authData: AuthData): Flow<Resource<HistoryResponse>>

    suspend fun getHistoryBalance(authData: AuthData): Flow<Resource<BalanceResponse>>

    suspend fun getDirectionStore(latLngData: LatLngData): Flow<Resource<DirectionResponse>>

    suspend fun getDirectionCustomer(directionData: DirectionData): Flow<Resource<DirectionResponse>>

    suspend fun depositOrWithDraw(
        authData: AuthData,
        deposit: Deposit
    ): Flow<Resource<DepositResponse>>

    suspend fun getDetailTrans(
        authData: AuthData,
        noTrans: String
    ): Flow<Resource<TransactionResponse>>
}