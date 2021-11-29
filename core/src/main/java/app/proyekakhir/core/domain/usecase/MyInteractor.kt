package app.proyekakhir.core.domain.usecase

import android.content.Context
import androidx.lifecycle.LiveData
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.local.LocalProperties
import app.proyekakhir.core.data.source.remote.response.account.AccountResponse
import app.proyekakhir.core.data.source.remote.response.balance.BalanceResponse
import app.proyekakhir.core.data.source.remote.response.balance.DepositResponse
import app.proyekakhir.core.data.source.remote.response.direction.DirectionResponse
import app.proyekakhir.core.data.source.remote.response.login.LoginResponse
import app.proyekakhir.core.data.source.remote.response.signup.SignUpResponse
import app.proyekakhir.core.data.source.remote.response.transaction.HistoryResponse
import app.proyekakhir.core.data.source.remote.response.transaction.TransactionResponse
import app.proyekakhir.core.domain.model.auth.Login
import app.proyekakhir.core.domain.model.auth.LoginData
import app.proyekakhir.core.domain.model.auth.SignUp
import app.proyekakhir.core.domain.model.auth.SignUpData
import app.proyekakhir.core.domain.model.balance.Deposit
import app.proyekakhir.core.domain.model.balance.DepositInput
import app.proyekakhir.core.domain.model.direction.DirectionData
import app.proyekakhir.core.domain.model.direction.LatLngData
import app.proyekakhir.core.domain.model.transaction.AuthData
import app.proyekakhir.core.domain.repository.IMyRepository
import app.proyekakhir.core.util.createPartFromString
import app.proyekakhir.core.util.prepareFile
import app.proyekakhir.core.util.prepareFilePart
import kotlinx.coroutines.flow.Flow

class MyInteractor(
    private val myRepository: IMyRepository,
    private val context: Context
) :
    MyUseCase {
    private var localProperties: LocalProperties = LocalProperties(context)

    override suspend fun loginDriver(loginData: LoginData): Flow<Resource<LoginResponse>> {
        val login = Login(
            loginData.fcmToken,
            loginData.phoneNumber
        )
        return myRepository.login(login)
    }

    override suspend fun signUpDriver(signUpData: SignUpData): LiveData<Resource<SignUpResponse>> {
        val signUp = SignUp(
            createPartFromString(signUpData.name_driver),
            createPartFromString(signUpData.email),
            createPartFromString(signUpData.j_kelamin),
            createPartFromString(signUpData.phone),
            createPartFromString(signUpData.plat_kendaraan),
            createPartFromString(signUpData.nik),
            createPartFromString(signUpData.nomor_stnk),
            context.prepareFile(signUpData.photo_profile),
            context.prepareFile(signUpData.photo_ktp),
            context.prepareFile(signUpData.photo_stnk)
        )
        return myRepository.signUp(signUp)
    }

    override suspend fun depositOrWithDraw(
        depositInput: DepositInput
    ): Flow<Resource<DepositResponse>> {
        val deposit = Deposit(
            createPartFromString(depositInput.nama),
            createPartFromString(depositInput.noRek),
            createPartFromString(depositInput.saldo),
            createPartFromString(depositInput.namaBank),
            createPartFromString(depositInput.type),
            context.prepareFilePart("image", depositInput.image)
        )
        return myRepository.depositOrWithDraw(
            AuthData(
                localProperties.apiToken!!,
                localProperties.fcmToken!!
            ), deposit
        )
    }

    override suspend fun getAccount(): Flow<Resource<AccountResponse>> {
        return myRepository.getAccount(
            AuthData(
                localProperties.apiToken!!,
                localProperties.fcmToken!!
            )
        )
    }

    override suspend fun acceptOrder(
        idTrans: Int
    ): Flow<Resource<TransactionResponse>> {
        return myRepository.acceptOrder(
            AuthData(
                localProperties.apiToken!!,
                localProperties.fcmToken!!
            ), idTrans
        )
    }

    override suspend fun validationCodeToStore(
        idTrans: Int,
        code: String
    ): Flow<Resource<TransactionResponse>> {
        return myRepository.validationCodeStore(
            AuthData(
                localProperties.apiToken!!,
                localProperties.fcmToken!!
            ), idTrans, code
        )
    }

    override suspend fun finishOrder(
        idTrans: Int
    ): Flow<Resource<TransactionResponse>> {
        return myRepository.finishOrder(
            AuthData(
                localProperties.apiToken!!,
                localProperties.fcmToken!!
            ), idTrans
        )
    }

    override suspend fun getCurrentOrder(): Flow<Resource<TransactionResponse>> {
        return myRepository.getCurrentOrder(
            AuthData(
                localProperties.apiToken!!,
                localProperties.fcmToken!!
            )
        )
    }

    override suspend fun getHistory(): Flow<Resource<HistoryResponse>> {
        return myRepository.getHistory(
            AuthData(
                localProperties.apiToken!!,
                localProperties.fcmToken!!
            )
        )
    }

    override suspend fun getHistoryBalance(): Flow<Resource<BalanceResponse>> {
        return myRepository.getHistoryBalance(
            AuthData(
                localProperties.apiToken!!,
                localProperties.fcmToken!!
            )
        )
    }

    override suspend fun getDirectionStore(directionData: DirectionData): Flow<Resource<DirectionResponse>> {
        val latLngData = LatLngData(
            StringBuilder().append(directionData.origin.latitude.toString()).append(",")
                .append(directionData.origin.longitude.toString()).toString(),
            StringBuilder().append(directionData.destination.latitude.toString()).append(",")
                .append(directionData.destination.longitude.toString()).toString()
        )
        return myRepository.getDirectionStore(latLngData)
    }

    override suspend fun getDirectionCustomer(directionData: DirectionData): Flow<Resource<DirectionResponse>> {
        return myRepository.getDirectionCustomer(directionData)
    }
}