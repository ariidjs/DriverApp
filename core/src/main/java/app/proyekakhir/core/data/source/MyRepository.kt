package app.proyekakhir.core.data.source

import app.proyekakhir.core.data.source.remote.RemoteDataSource
import app.proyekakhir.core.domain.model.auth.Login
import app.proyekakhir.core.domain.model.auth.SignUp
import app.proyekakhir.core.domain.model.balance.Deposit
import app.proyekakhir.core.domain.model.direction.DirectionData
import app.proyekakhir.core.domain.model.direction.LatLngData
import app.proyekakhir.core.domain.model.transaction.AuthData
import app.proyekakhir.core.domain.repository.IMyRepository

class MyRepository(
    private val remoteDataSource: RemoteDataSource
) : IMyRepository {
    override suspend fun login(login: Login) = remoteDataSource.login(login)
    override suspend fun signUp(signUp: SignUp) = remoteDataSource.signUpDriver(signUp)
    override suspend fun getAccount(authData: AuthData) = remoteDataSource.getAccount(authData)
    override suspend fun acceptOrder(authData: AuthData, idTrans: Int) =
        remoteDataSource.acceptOrder(authData, idTrans)

    override suspend fun declineOrder(
        authData: AuthData,
        idTrans: Int
    ) = remoteDataSource.declineOrder(authData, idTrans)

    override suspend fun validationCodeStore(
        authData: AuthData,
        idTrans: Int,
        code: String
    ) = remoteDataSource.validationToStore(authData, idTrans, code)

    override suspend fun finishOrder(
        authData: AuthData,
        idTrans: Int
    ) = remoteDataSource.finishOrder(authData, idTrans)

    override suspend fun getCurrentOrder(
        authData: AuthData
    ) = remoteDataSource.getCurrentOrder(authData)

    override suspend fun getHistory(authData: AuthData) =
        remoteDataSource.getHistory(authData)

    override suspend fun getHistoryBalance(authData: AuthData) =
        remoteDataSource.getHistoryBalance(authData)

    override suspend fun getDirectionStore(latLngData: LatLngData) =
        remoteDataSource.getDirectionStore(latLngData)

    override suspend fun getDirectionCustomer(directionData: DirectionData) =
        remoteDataSource.getDirectionCustomer(directionData)

    override suspend fun depositOrWithDraw(
        authData: AuthData,
        deposit: Deposit
    ) = remoteDataSource.depositOrWithDraw(authData, deposit)

    override suspend fun getDetailTrans(
        authData: AuthData,
        noTrans: String
    ) = remoteDataSource.getDetailTrans(authData, noTrans)
}