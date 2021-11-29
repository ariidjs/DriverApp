package app.proyekakhir.core.data.source.remote.network

import app.proyekakhir.core.data.Resource
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

abstract class BaseApi {
    suspend fun <T> flowApiCall(call: suspend () -> T): Flow<Resource<T>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                emit(Resource.Success(call.invoke()))
                emit(Resource.Loading(false))
            } catch (t: Throwable) {

                when (t) {
                    is HttpException -> {
                        emit(
                            Resource.Error(
                                false,
                                t.code(),
                                t.response()?.errorBody()
                            )
                        )
                        emit(Resource.Loading(false))
                    }
                    else -> {
                        emit(Resource.Error(true, null, null))
                        emit(Resource.Loading(false))
                    }
                }
            }
        }.flowOn(IO)
    }
}