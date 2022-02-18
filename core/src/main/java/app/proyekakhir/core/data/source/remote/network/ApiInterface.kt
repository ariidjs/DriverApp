package app.proyekakhir.core.data.source.remote.network

import app.proyekakhir.core.data.source.remote.response.account.AccountResponse
import app.proyekakhir.core.data.source.remote.response.balance.BalanceResponse
import app.proyekakhir.core.data.source.remote.response.balance.DepositResponse
import app.proyekakhir.core.data.source.remote.response.login.LoginResponse
import app.proyekakhir.core.data.source.remote.response.signup.SignUpResponse
import app.proyekakhir.core.data.source.remote.response.transaction.HistoryResponse
import app.proyekakhir.core.data.source.remote.response.transaction.TransactionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiInterface {

    @POST("driver/login/{phonenumber}")
    suspend fun loginDriver(
        @Header("fcm") fcm: String,
        @Path("phonenumber") phoneNumber: String
    ): LoginResponse

    @Multipart
    @POST("driver/register")
    suspend fun signUpDriver(
        @Part("name_driver") nama: RequestBody,
        @Part("email") email: RequestBody,
        @Part("j_kelamin") jk: RequestBody,
        @Part("phone") noHp: RequestBody,
        @Part("plat_kendaraan") noKendaraan: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("nomor_stnk") noStnk: RequestBody,
        @Part fotoStnk: MultipartBody.Part?,
        @Part fotoKtp: MultipartBody.Part?,
        @Part fotoFormal: MultipartBody.Part?
    ): SignUpResponse


    @GET("driver")
    suspend fun getAccount(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String
    ): AccountResponse

    @FormUrlEncoded
    @POST("driver/confirmorder/{idTrans}")
    suspend fun acceptOrder(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String,
        @Path("idTrans") idTrans: Int,
        @Field("status") status: Int = 4
    ): TransactionResponse

    @FormUrlEncoded
    @POST("driver/confirmorder/{idTrans}")
    suspend fun declineOrder(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String,
        @Path("idTrans") idTrans: Int,
        @Field("status") status: Int = 3
    ): TransactionResponse

    @GET("driver/transaction/{idTrans}/{kode}")
    suspend fun verificationCode(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String,
        @Path("idTrans") idTrans: Int,
        @Path("kode") kode: String
    ): TransactionResponse


    @GET("driver/transaction/{idTrans}")
    suspend fun finishOrder(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String,
        @Path("idTrans") idTrans: Int
    ): TransactionResponse

    @GET("driver/current")
    suspend fun getCurrentOrder(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String
    ): TransactionResponse

    @GET("driver/history")
    suspend fun getHistory(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String
    ): HistoryResponse

    @GET("driver/saldo/history")
    suspend fun getHistoryBalance(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String
    ): BalanceResponse

    @Multipart
    @POST("driver/withdrawordeposit")
    suspend fun depositOrWithDraw(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String,
        @Part("nama") nama: RequestBody,
        @Part("norek") noRek: RequestBody,
        @Part("saldo") saldo: RequestBody,
        @Part("nama_bank") namaBank: RequestBody,
        @Part("type") type: RequestBody,
        @Part image: MultipartBody.Part?,
    ): DepositResponse

    @GET("driver/detailTransaction/{noTrans}")
    suspend fun getDetailTrans(
        @Header("Authorization") token: String,
        @Header("fcm") fcm: String,
        @Path("noTrans") noTrans: String
    ): TransactionResponse
}