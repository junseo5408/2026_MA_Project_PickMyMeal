package com.nnine.pickmymeal.data.network

import com.nnine.pickmymeal.data.model.KakaoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoLocalApi {

    // 카테고리 기반 검색 (무작위 - 전체 음식점)
    @GET("v2/local/search/category.json")
    suspend fun searchByCategory(
        @Header("Authorization") apiKey: String,
        @Query("category_group_code") categoryCode: String = "FD6",
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("radius") radius: Int = 500,
        @Query("size") size: Int = 15
    ): KakaoSearchResponse

    // 키워드 기반 검색 (한식, 중식 등 특정 카테고리)
    @GET("v2/local/search/keyword.json")
    suspend fun searchByKeyword(
        @Header("Authorization") apiKey: String,
        @Query("query") keyword: String,
        @Query("category_group_code") categoryCode: String = "FD6",
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("radius") radius: Int = 500,
        @Query("size") size: Int = 15
    ): KakaoSearchResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://dapi.kakao.com/"

    val api: KakaoLocalApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(KakaoLocalApi::class.java)
    }
}
