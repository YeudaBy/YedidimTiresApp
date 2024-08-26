package com.yeudaby.yedidim.tires

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query


interface GovIlApi {
    @GET("api/3/action/datastore_search")
    suspend fun carDetails(
        @Query("resource_id") resourceId: String,
        @Query("q") query: Long,
        @Query("limit") limit: Int = 10
    ): BaseResult
}

@JsonClass(generateAdapter = true)
data class BaseResult(
    val success: Boolean,
    val result: Result?
)

@JsonClass(generateAdapter = true)
data class Result(
    val records: List<Record>,
    val total: Int
)

@JsonClass(generateAdapter = true)
data class Record(
    val _id: Int,
    val mispar_rechev: Long,
    val tozeret_nm: String?,
    val kinuy_mishari: String,
    val ramat_gimur: String,
    val shnat_yitzur: Int,
    val tzeva_rechev: String,
    val zmig_kidmi: String,
    val zmig_ahori: String,
) {
    companion object {
        fun empty(carNumber: Long) = Record(
            _id = 0,
            mispar_rechev = carNumber,
            tozeret_nm = null,
            kinuy_mishari = "",
            ramat_gimur = "",
            shnat_yitzur = 0,
            tzeva_rechev = "",
            zmig_kidmi = "",
            zmig_ahori = "",
        )
    }
}