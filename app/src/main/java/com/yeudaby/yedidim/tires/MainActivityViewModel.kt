package com.yeudaby.yedidim.tires

import android.content.Context
import androidx.lifecycle.ViewModel
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivityViewModel(
    context: Context
): ViewModel() {
    private var okHttpClient: OkHttpClient = OkHttpClient()
        .newBuilder()
        .addInterceptor(ChuckerInterceptor(context))
        .build()
    private var moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private var retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    private var api = retrofit.create(GovIlApi::class.java)

    private var _carDetails = MutableStateFlow<List<Record>?>(null)
    val carDetails = _carDetails.asStateFlow()

    private var _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private var _error = MutableStateFlow<Throwable?>(null)
    val error = _error.asStateFlow()

    suspend fun fetchCarDetails(query: Long) = runCatching{
        _loading.value = true
        val results = mutableListOf<Record>()
        for (resource in resources) {
            val response = api.carDetails(resource, query).result
            if (response != null) {
                results.addAll(response.records)
            }
        }
        println(results)
        _carDetails.value = results
    }.onFailure {
        _error.value = it
        _loading.value = false
    }.onSuccess {
        _loading.value = false
    }

    fun clearResults() {
        _carDetails.value = null
    }

    companion object {
        private const val BASE_URL = "https://data.gov.il/"
        private val resources = listOf(
            "053cea08-09bc-40ec-8f7a-156f0677aff3"
        )
    }
}