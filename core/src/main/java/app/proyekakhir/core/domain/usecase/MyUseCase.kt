package app.proyekakhir.core.domain.usecase

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
import app.proyekakhir.core.domain.model.auth.LoginData
import app.proyekakhir.core.domain.model.auth.SignUpData
import app.proyekakhir.core.domain.model.balance.DepositInput
import app.proyekakhir.core.domain.model.direction.DirectionData
import kotlinx.coroutines.flow.Flow

interface MyUseCase {
    suspend fun loginDriver(loginData: LoginData): Flow<Resource<LoginResponse>>

    suspend fun signUpDriver(signUpData: SignUpData): LiveData<Resource<SignUpResponse>>

    suspend fun getAccount(): Flow<Resource<AccountResponse>>

    suspend fun acceptOrder(idTrans: Int): Flow<Resource<TransactionResponse>>

    suspend fun declineOrder(idTrans: Int): Flow<Resource<TransactionResponse>>

    suspend fun validationCodeToStore(
        idTrans: Int,
        code: String
    ): Flow<Resource<TransactionResponse>>

    suspend fun finishOrder(idTrans: Int): Flow<Resource<TransactionResponse>>

    suspend fun getCurrentOrder(): Flow<Resource<TransactionResponse>>

    suspend fun getHistory(): Flow<Resource<HistoryResponse>>

    suspend fun getHistoryBalance(): Flow<Resource<BalanceResponse>>

    suspend fun getDirectionStore(directionData: DirectionData): Flow<Resource<DirectionResponse>>

    suspend fun getDirectionCustomer(directionData: DirectionData): Flow<Resource<DirectionResponse>>

    suspend fun depositOrWithDraw(depositInput: DepositInput): Flow<Resource<DepositResponse>>

    suspend fun getDetailTrans(
        noTrans: String
    ): Flow<Resource<TransactionResponse>>
}