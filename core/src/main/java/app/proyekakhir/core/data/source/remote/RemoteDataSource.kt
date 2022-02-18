package app.proyekakhir.core.data.source.remote

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.proyekakhir.core.BuildConfig.MAPS_API_KEY
import app.proyekakhir.core.data.Resource
import app.proyekakhir.core.data.source.remote.network.ApiInterface
import app.proyekakhir.core.data.source.remote.network.ApiMapsInterface
import app.proyekakhir.core.data.source.remote.network.BaseApi
import app.proyekakhir.core.data.source.remote.network.ProgressRequestBody
import app.proyekakhir.core.data.source.remote.response.account.AccountResponse
import app.proyekakhir.core.data.source.remote.response.login.LoginResponse
import app.proyekakhir.core.data.source.remote.response.signup.SignUpResponse
import app.proyekakhir.core.domain.model.auth.Login
import app.proyekakhir.core.domain.model.auth.SignUp
import app.proyekakhir.core.domain.model.balance.Deposit
import app.proyekakhir.core.domain.model.direction.DirectionData
import app.proyekakhir.core.domain.model.direction.LatLngData
import app.proyekakhir.core.domain.model.transaction.AuthData
import app.proyekakhir.core.util.Constants.DIRECTION_MODE
import app.proyekakhir.core.util.Constants.ROUTING_PREF
import app.proyekakhir.core.util.createPart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RemoteDataSource(
    private val apiInterface: ApiInterface,
    private val apiMapsInterface: ApiMapsInterface
) :
    BaseApi(), ProgressRequestBody.UploadCallbacks {


    suspend fun login(login: Login): Flow<Resource<LoginResponse>> {
        return flowApiCall { apiInterface.loginDriver(login.fcmToken, login.phoneNumber) }
    }

    val result = MutableLiveData<Resource<SignUpResponse>>()
    suspend fun signUpDriver(signUp: SignUp): LiveData<Resource<SignUpResponse>> {
        val stnkRequestBody =
            ProgressRequestBody(
                signUp.photo_stnk,
                this@RemoteDataSource,
                Uri.fromFile(signUp.photo_stnk).toString(),
                "Foto STNK"
            )
        val ktpRequestBody =
            ProgressRequestBody(
                signUp.photo_ktp,
                this@RemoteDataSource,
                Uri.fromFile(signUp.photo_ktp).toString(),
                "Foto KTP"
            )
        val avatarRequestBody = ProgressRequestBody(
            signUp.photo_profile,
            this@RemoteDataSource,
            Uri.fromFile(signUp.photo_profile).toString(), "Foto Formal"
        )
        CoroutineScope(IO).launch {
            try {
                val response = apiInterface.signUpDriver(
                    signUp.name_driver,
                    signUp.email,
                    signUp.j_kelamin,
                    signUp.phone,
                    signUp.plat_kendaraan,
                    signUp.nik,
                    signUp.nomor_stnk,
                    createPart("photo_stnk", signUp.photo_stnk.name, stnkRequestBody),
                    createPart("photo_ktp", signUp.photo_ktp.name, ktpRequestBody),
                    createPart("photo_profile", signUp.photo_profile.name, avatarRequestBody)
                )
                result.postValue(Resource.Success(response))

            } catch (t: Throwable) {
                when (t) {
                    is HttpException -> result.postValue(
                        Resource.Error(
                            false,
                            t.code(),
                            t.response()?.errorBody()
                        )
                    )
                    else -> result.postValue(Resource.Error(true, null, null))
                }
            }
        }
        return result

    }

    suspend fun getAccount(authData: AuthData): Flow<Resource<AccountResponse>> {
        return flowApiCall { apiInterface.getAccount("Bearer " + authData.token, authData.fcm) }
    }

    suspend fun acceptOrder(authData: AuthData, idTrans: Int) = flowApiCall {
        apiInterface.acceptOrder("Bearer " + authData.token, authData.fcm, idTrans)
    }

    suspend fun declineOrder(authData: AuthData, idTrans: Int) = flowApiCall {
        apiInterface.declineOrder("Bearer " + authData.token, authData.fcm, idTrans)
    }

    suspend fun validationToStore(authData: AuthData, idTrans: Int, code: String) = flowApiCall {
        apiInterface.verificationCode("Bearer " + authData.token, authData.fcm, idTrans, code)
    }

    suspend fun finishOrder(authData: AuthData, idTrans: Int) = flowApiCall {
        apiInterface.finishOrder("Bearer " + authData.token, authData.fcm, idTrans)
    }

    suspend fun getCurrentOrder(authData: AuthData) = flowApiCall {
        apiInterface.getCurrentOrder("Bearer " + authData.token, authData.fcm)
    }

    suspend fun getHistory(authData: AuthData) = flowApiCall {
        apiInterface.getHistory("Bearer " + authData.token, authData.fcm)
    }

    suspend fun getHistoryBalance(authData: AuthData) = flowApiCall {
        apiInterface.getHistoryBalance("Bearer " + authData.token, authData.fcm)
    }


    suspend fun getDirectionStore(latLngData: LatLngData) = flowApiCall {
        apiMapsInterface.getDirections(
            MAPS_API_KEY,
            DIRECTION_MODE,
            ROUTING_PREF,
            latLngData.origin,
            latLngData.destination
        )

    }

    suspend fun getDirectionCustomer(directionData: DirectionData) = flowApiCall {
        apiMapsInterface.getDirections(
            MAPS_API_KEY,
            DIRECTION_MODE,
            ROUTING_PREF,
            StringBuilder().append(directionData.origin.latitude).append(" ,")
                .append(directionData.origin.longitude).toString(),
            StringBuilder().append(directionData.destination.latitude).append(" ,")
                .append(directionData.destination.longitude).toString()
        )
    }

    suspend fun depositOrWithDraw(authData: AuthData, deposit: Deposit) = flowApiCall {
        apiInterface.depositOrWithDraw(
            "Bearer " + authData.token,
            authData.fcm,
            deposit.nama,
            deposit.noRek,
            deposit.saldo,
            deposit.namaBank,
            deposit.type,
            deposit.image
        )
    }

    suspend fun getDetailTrans(authData: AuthData, noTrans: String) = flowApiCall {
        apiInterface.getDetailTrans("Bearer " + authData.token, authData.fcm, noTrans)
    }


    /*
    Progress Upload
     */

    override fun onProgressUpdate(percentage: Int, name: String) {
        result.postValue(Resource.Loading(true, percentage, name))
    }

    override fun onError() {
        result.postValue(Resource.Error(false, 401, null))
    }

    override fun onFinish() {
        result.postValue(Resource.Loading(true, 100))
    }
}